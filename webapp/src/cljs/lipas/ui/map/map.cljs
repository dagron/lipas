(ns lipas.ui.map.map
  (:require proj4
            ["ol"]
            [clojure.string :as string]
            [goog.object :as gobj]
            [lipas.ui.map.events :as events]
            [lipas.ui.map.subs :as subs]
            [lipas.ui.mui :as mui]
            [lipas.ui.svg :as svg]
            [lipas.ui.utils :refer [<== ==>] :as utils]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

;; (set! *warn-on-infer* true)

(def temp-fid-prefix "temp")

(def circle-style (ol.style.Style.
                   #js{:image
                       (ol.style.Circle.
                        #js{:radius 10
                            :stroke (ol.style.Stroke
                                     #js{:color mui/primary})
                            :fill   (ol.style.Fill.
                                     #js{:color mui/secondary2})})}))


(def circle-style2 (ol.style.Style.
                    #js{:image
                        (ol.style.Circle.)}))

(defn ->marker-style [opts]
  (ol.style.Style.
   #js{:image
       (ol.style.Icon.
        #js{:src    (str "data:image/svg+xml;charset=utf-8,"
                         (-> opts
                             svg/->marker-str
                             js/encodeURIComponent))
            :anchor #js[0.5 0.85]
            :offset #js[0 0]})}))

(def blue-marker-style (->marker-style {}))
(def red-marker-style (->marker-style {:color mui/secondary}))

(def default-stroke (ol.style.Stroke. #js{:color "#3399CC" :width 3}))
(def default-fill (ol.style.Fill. #js{:color "rgba(255,255,0,0.4)"}))
(def hover-stroke (ol.style.Stroke. #js{:color "rgba(255,0,0,0.4)" :width 3.5}))
(def hover-fill (ol.style.Fill. #js{:color "rgba(255,0,0,0.4)"}))

(def vertices-style
  (ol.style.Style.
   #js{:image
       (ol.style.Circle.
        #js{:radius 5
            :stroke (ol.style.Stroke
                     #js{:color mui/primary})
            :fill   (ol.style.Fill.
                     #js{:color mui/secondary2})})
       :geometry (fn [f]
                   (let [geom-type (-> f .getGeometry .getType)
                         coords    (case geom-type
                                     "Polygon"    (-> f .getGeometry .getCoordinates first)
                                     "LineString" (-> f .getGeometry .getCoordinates)
                                     nil)]
                     (when coords
                       (ol.geom.MultiPoint. coords))))}))

(def edit-style
  (ol.style.Style.
   #js{:stroke
       (ol.style.Stroke.
        #js{:width 3
            :color "blue"})
       :fill default-fill
       :image
       (ol.style.Circle.
        #js{:radius 5
            :fill   default-fill
            :stroke default-stroke})}))

(def default-style
  (ol.style.Style.
   #js{:stroke default-stroke
       :fill default-fill
       :image
       (ol.style.Circle.
        #js{:radius 5
            :fill   default-fill
            :stroke default-stroke})}))

(def hover-style
  (ol.style.Style.
   #js{:stroke hover-stroke
       :fill   default-fill
       :image  (ol.style.Circle.
                #js{:radius 7
                    :fill   default-fill
                    :stroke hover-stroke})}))

(def hover-styles #js[hover-style blue-marker-style])

(js/proj4.defs "EPSG:3067" (str "+proj=utm"
                                "+zone=35"
                                "+ellps=GRS80"
                                "+towgs84=0,0,0,0,0,0,0"
                                "+units=m"
                                "+no_defs"))

(let [^js/ol ol                  ol
      ^js/ol.proj ol-proj        (.-proj ol)
      ^js/ol.proj.proj4 ol-proj4 (.-proj4 ol-proj)]
  (.register ol-proj4 proj4))

(def mml-resolutions
  #js[8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25])

(def mml-matrix-ids (clj->js (range (count mml-resolutions))))

(def ^js/ol.proj.Projection epsg3067 (ol.proj.get "EPSG:3067"))
(def ^js/ol.Extent epsg3067-extent #js[-548576.0 6291456.0 1548576.0 8388608.0])

(.setExtent epsg3067 epsg3067-extent)

(def epsg3067-top-left (ol.extent.getTopLeft (.getExtent epsg3067)))

(def jyvaskyla #js[435047 6901408])
(def center-wgs84 (ol.proj.fromLonLat #js[24 65]))

(def geoJSON (ol.format.GeoJSON. #js{:dataProjection    "EPSG:4326"
                                     :featureProjection "EPSG:3067"}))

(defn ->ol-features [geoJSON-features]
  (.readFeatures geoJSON geoJSON-features))

(defn ->geoJSON [ol-features]
  (.writeFeaturesObject geoJSON ol-features))

(defn ->clj [x]
  (js->clj x :keywordize-keys true))

(def ->geoJSON-clj (comp ->clj ->geoJSON))

(defn ->wmts-url [layer-name]
  (str "/mapproxy/wmts/"
       layer-name
       "/{TileMatrixSet}/{TileMatrix}/{TileCol}/{TileRow}.png"))

(def urls
  {:taustakartta (->wmts-url "mml_taustakartta")
   :maastokartta (->wmts-url "mml_maastokartta")
   :ortokuva     (->wmts-url "mml_ortokuva")})

(defn ->wmts [{:keys [url layer-name visible?]
               :or   {visible? false}}]
  (ol.layer.Tile.
   #js{:visible visible?
       :source
       (ol.source.WMTS.
        #js{:url             url
            :layer           layer-name
            :projection      "EPSG:3067"
            :matrixSet       "mml_grid"
            :tileGrid        (ol.tilegrid.WMTS.
                              #js{:origin      epsg3067-top-left
                                  :extent      epsg3067-extent
                                  :resolutions mml-resolutions
                                  :matrixIds   mml-matrix-ids})
            :format          "png"
            :requestEncoding "REST"
            :isBaseLayer     true})}))


(defn init-layers []
  {:basemaps
   {:taustakartta (->wmts {:url        (:taustakartta urls)
                           :layer-name "MML-Taustakartta"
                           :visible?   true})
    :maastokartta (->wmts {:url        (:maastokartta urls)
                           :layer-name "MML-Maastokartta"})
    :ortokuva     (->wmts {:url        (:ortokuva urls)
                           :layer-name "MML-Ortokuva"})}
   :overlays
   {:vectors (ol.layer.Vector.
              #js{:source (ol.source.Vector.)
                  :style  default-style})
    :edits   (ol.layer.Vector.
              #js{:source (ol.source.Vector.)
                  :style  #js[edit-style vertices-style]})}})

(defn init-map! [{:keys [center zoom]}]
  (let [layers (init-layers)
        view   (ol.View. #js{:center      #js[(:lon center) (:lat center)]
                             :zoom        zoom
                             :projection  "EPSG:3067"
                             :resolutions mml-resolutions
                             :units       "m"})

        popup-overlay (ol.Overlay. #js{:offset #js[-15 0]
                                       :element
                                       (js/document.getElementById "popup-anchor")})

        opts #js {:target   "map"
                  :layers   #js[(-> layers :basemaps :taustakartta)
                                (-> layers :basemaps :maastokartta)
                                (-> layers :basemaps :ortokuva)
                                (-> layers :overlays :vectors)
                                (-> layers :overlays :edits)]
                  :overlays #js[popup-overlay]
                  :view     view}

        hover (ol.interaction.Select.
               #js{:layers    [(-> layers :overlays :vectors)]
                   ;;:style     blue-marker-style
                   :style     hover-style
                   :condition ol.events.condition.pointerMove})

        select (ol.interaction.Select.
                #js{:layers #js[(-> layers :overlays :vectors)]
                    :style hover-style})

        lmap (ol.Map. opts)]

    (.on hover "select"
         (fn [e]
           (let [coords   (gobj/getValueByKeys e "mapBrowserEvent" "coordinate")
                 selected (gobj/get e "selected")]
             (.setPosition popup-overlay coords)
             (==> [::events/show-popup (when (not-empty selected)
                                         {:anchor-el (.getElement popup-overlay)
                                          :data      (-> selected ->geoJSON-clj)})]))))

    (.on select "select"
         (fn [e]
           (let [coords   (gobj/getValueByKeys e "mapBrowserEvent" "coordinate")
                 selected (aget (gobj/get e "selected") 0)]
             (.setPosition popup-overlay coords)
             (==> [::events/show-sports-site
                   (when selected
                     (.get selected "lipas-id"))]))))

    (.on lmap "moveend"
         (fn [e]
           (let [lon  (aget (.getCenter view) 0)
                 lat  (aget (.getCenter view) 1)
                 zoom (.getZoom view)]
             (==> [::events/set-view lat lon zoom]))))

    {:lmap          lmap
     :view          view
     :center        center
     :zoom          zoom
     :layers        layers
     ;; We don't re-create :hover and :select each time when we toggle
     ;; them because it causes buggy behavior. We keep refs to
     ;; singleton instances under special :interactions* key in
     ;; map-ctx where we can find them when they need to be enabled.
     :interactions* {:select select
                     :hover  hover}
     :overlays      {:popup popup-overlay}}))

;; Popups are rendered 'outside' OpenLayers by React so we need to
;; inform the outside world.
(defn clear-popup! [map-ctx]
  (==> [::events/show-popup nil])
  map-ctx)

(defn update-geoms! [{:keys [layers] :as map-ctx} geoms]
  (let [vectors (-> layers :overlays :vectors)
        source  (.getSource vectors)]
    (.clear source)
    (doseq [g    geoms
            :let [f (-> g
                        clj->js
                        ->ol-features)]]
      (.addFeatures source f))
    (assoc map-ctx :geoms geoms)))

(defn set-basemap! [{:keys [layers] :as map-ctx} basemap]
  (doseq [[k v] (:basemaps layers)
          :let  [visible? (= k basemap)]]
    (.setVisible v visible?))
  map-ctx)

(defn select-features! [{:keys [interactions] :as map-ctx} features]
  (let [select (-> interactions :select)]
    (doto (.getFeatures select)
      (.clear)
      (.extend features))
    map-ctx))

(defn find-feature-by-id [{:keys [layers]} fid]
  (let [layer  (-> layers :overlays :vectors)
        source (.getSource layer)]
    (.getFeatureById source fid)))

(defn find-features-by-lipas-id [{:keys [layers]} lipas-id]
  (let [layer  (-> layers :overlays :vectors)
        source (.getSource layer)
        res    #js[]]
    (.forEachFeature source
                     (fn [f]
                       (when (-> (.getId f)
                                 (string/split "-")
                                 first
                                 (= (str lipas-id)))
                         (.push res f))
                       ;; Iteration stops if truthy val is returned
                       ;; but we want to find all matching features so
                       ;; nil is returned.
                       nil))
    res))

(defn select-sports-site! [map-ctx lipas-id]
  ;; First feature in f-coll has suffix "-0"
  (if-let [features (not-empty (find-features-by-lipas-id map-ctx lipas-id))]
    (select-features! map-ctx features)
    map-ctx))

;; The snap interaction must be added after the Modify and Draw
;; interactions in order for its map browser event handlers to be
;; fired first. Its handlers are responsible of doing the snapping.
(defn enable-snapping! [{:keys [^js/ol.Map lmap layers] :as map-ctx}]
  (prn "Enable snapping!")
  (let [source (-> layers :overlays :edits .getSource)
        snap   (ol.interaction.Snap. #js{:source source})]
    (.addInteraction lmap snap)
    (assoc-in map-ctx [:interactions :snap] snap)))

(defn enable-splitter! [{:keys [^js/ol.Map lmap layers] :as map-ctx}]
  (prn "Not Enabling splitter!")
  ;; TODO figure out what's wrong
  ;; (let [source   (-> layers :overlays :edits .getSource)
  ;;       _ (prn "source...")
  ;;       _ (js/console.log (.getFeaturesCollection source))
  ;;       splitter (ol.interaction.Splitter. #js{:source source})]
  ;;   (.addInteraction lmap splitter)
  ;;   (assoc-in map-ctx [:interactions :splitter] splitter)
  ;;   )
  map-ctx
  )

(defn start-editing! [{:keys [^js/ol.Map lmap layers] :as map-ctx}
                      geoJSON-feature on-modifyend]
  (prn "Start editing")
  (let [layer     (-> layers :overlays :edits)
        source    (.getSource layer)
        features  (-> geoJSON-feature clj->js ->ol-features)
        _         (.addFeatures source features)
        modify    (ol.interaction.Modify. #js{:source source})
        geom-type (-> geoJSON-feature :features first :geometry :type)]

    ;; Splitter needs to be added before other interactions
    (let [new-ctx (if (#{"LineString" "Polygon"} geom-type)
                    (enable-splitter! map-ctx)
                    map-ctx)]

      (.addInteraction lmap modify)
      (.on modify "modifyend"
           (fn [e]
             (on-modifyend (->geoJSON-clj (.getFeatures source)))))

      (-> new-ctx
          (assoc-in [:interactions :modify] modify)
          enable-snapping!))))

(defn start-editing-site! [{:keys [layers] :as map-ctx} lipas-id on-modifyend]
  (let [layer    (-> layers :overlays :vectors)
        source   (.getSource layer)
        features (find-features-by-lipas-id map-ctx lipas-id)]
    (js/console.log features)
    ;; Remove from original source so we won't display duplicate when
    ;; feature is added to :edits layer.
    (.forEach features
              (fn [f]
                (.removeFeature source f)))
    (start-editing! map-ctx (->geoJSON-clj features) on-modifyend)))

(defn start-drawing! [{:keys [^js/ol.Map lmap layers]
                       :as   map-ctx} geom-type on-draw-end]
  (prn "Start drawing!")
  (let [layer  (-> layers :overlays :edits)
        source (.getSource layer)
        draw   (ol.interaction.Draw. #js{:source source
                                         :type   geom-type})]
    ;; Splitter needs to be enabled before other interactions
    (let [new-ctx (if  (#{"LineString" "Polygon"} geom-type)
                    (enable-splitter! map-ctx)
                    map-ctx)]

      (.addInteraction lmap draw)
      (.on draw "drawend"
           (fn [e]
             (let [f (gobj/get e "feature")
                   _ (.setId f (str (gensym temp-fid-prefix)))]
               (.addFeature source f)
               (on-draw-end (->geoJSON-clj (.getFeatures source))))))

      (-> new-ctx
          (assoc-in [:interactions :draw] draw)
          enable-snapping!))))

(defn update-center! [{:keys [^js/ol.View view] :as map-ctx}
                      {:keys [lon lat] :as center}]
  (.setCenter view #js[lon lat])
  (assoc map-ctx :center center))

(defn update-zoom! [{:keys [^js/ol.View view] :as map-ctx} zoom]
  (.setZoom view zoom)
  (assoc map-ctx :zoom zoom))

(defn show-feature! [{:keys [layers] :as map-ctx} geoJSON-feature]
  (let [vectors (-> layers :overlays :edits)
        source  (.getSource vectors)
        fs      (-> geoJSON-feature clj->js ->ol-features)]
    (.addFeatures source fs)
    map-ctx))

(defn clear-interactions! [{:keys [^js/ol.Map lmap interactions interactions*]
                            :as   map-ctx}]
  (prn "Clear interactions!")
  ;; Special treatment for 'singleton' interactions*. OpenLayers
  ;; doesn't treat 'copies' identical to original ones. Therefore we
  ;; need to pass the original ones explicitly.
  (doseq [v     (vals (merge interactions interactions*))
          :when (some? v)]
    (.removeInteraction lmap v))

  (assoc map-ctx :interactions {}))

(defn- existing-feature? [f]
  (-> (.getId f)
      (string/starts-with? temp-fid-prefix)
      not))

(defn clear-edits! [{:keys [layers] :as map-ctx}]
  (let [edits-source (-> layers :overlays :edits .getSource)
        geoms-source (-> layers :overlays :vectors .getSource)]
    ;; We add existing features which have been transferred to :edits
    ;; layer back to :vectors layer.
    (.forEachFeature edits-source
                     (fn [f]
                       (when (existing-feature? f)
                         (.addFeature geoms-source f))))
    (.clear edits-source)
    map-ctx))

;; Adding new features
(defn set-adding-mode! [map-ctx mode]
  (let [map-ctx (clear-interactions! map-ctx)]
    (case (:sub-mode mode)
      :drawing  (start-drawing! map-ctx (:geom-type mode)
                                (fn [f] (==> [::events/new-geom-drawn f])))
      :editing  (start-editing! map-ctx (:geom mode)
                                (fn [f] (==> [::events/update-new-geom f])))
      :finished (show-feature! map-ctx (:geom mode)))))

(defn update-adding-mode! [map-ctx mode]
  (let [old-mode (:mode map-ctx)]
    (if (= (:sub-mode mode) (:sub-mode old-mode))
      map-ctx ;; Noop
      (set-adding-mode! map-ctx mode))))

(defn continue-editing! [{:keys [layers] :as map-ctx} on-modifyend]
  (prn "Continue editing!")
  (let [layer (-> layers :overlays :edits)
        fs    (-> layer .getSource .getFeatures ->geoJSON-clj)]
    (start-editing! map-ctx fs on-modifyend)))

;; Editing existing features
(defn set-editing-mode!
  ([map-ctx mode]
   (set-editing-mode! map-ctx mode false))
  ([map-ctx {:keys [lipas-id geom-type sub-mode]} continue?]
   (let [map-ctx (clear-interactions! map-ctx)]
     (let [on-modifyend (fn [f]
                          (==> [::events/update-geometries lipas-id f])
                          (when (= :drawing sub-mode)
                            ;; Switch back to editing mode after drawing
                            (==> [::events/start-editing lipas-id :editing geom-type])))]
       (case sub-mode
         :drawing (start-drawing! map-ctx geom-type on-modifyend)
         :editing (if continue?
                    (continue-editing! map-ctx on-modifyend)
                    (start-editing-site! map-ctx lipas-id on-modifyend)))))))

(defn update-editing-mode! [map-ctx mode]
  (let [old-mode (:mode map-ctx)]
    (if (= (:sub-mode mode) (:sub-mode old-mode))
      map-ctx ;; Noop
      (set-editing-mode! map-ctx mode :continue))))

(defn enable-hover! [{:keys [^js/ol.Map lmap interactions*] :as map-ctx}]
  (let [hover (:hover interactions*)]
    (.addInteraction lmap hover)
    (assoc-in map-ctx [:interactions :hover] hover)))

(defn enable-select! [{:keys [^js/ol.Map lmap interactions*] :as map-ctx}]
  (let [select (:select interactions*)]
    (.addInteraction lmap select)
    (assoc-in map-ctx [:interactions :select] select)))

;; Browsing and selecting features
(defn set-default-mode! [map-ctx mode]
  (let [map-ctx (-> map-ctx
                    clear-edits!
                    clear-interactions!
                    enable-hover!
                    enable-select!)]
    (if-let [lipas-id (:lipas-id mode)]
      (select-sports-site! map-ctx lipas-id)
      map-ctx)))

(defn update-default-mode! [map-ctx mode]
  (if-let [lipas-id (:lipas-id mode)]
    (select-sports-site! map-ctx lipas-id)
    map-ctx))

(defn set-mode! [map-ctx mode]
  (let [mode (case (:name mode)
               :default (set-default-mode! map-ctx mode)
               :editing (set-editing-mode! map-ctx mode)
               :adding  (set-adding-mode! map-ctx mode))]
    (assoc map-ctx :mode mode)))

(defn update-mode! [map-ctx mode]
  (let [update? (= (-> map-ctx :mode :name) (:name mode))
        map-ctx (case (:name mode)
                  :default (if update?
                             (update-default-mode! map-ctx mode)
                             (set-default-mode! map-ctx mode))
                  :editing (if update?
                             (update-editing-mode! map-ctx mode)
                             (set-editing-mode! map-ctx mode))
                  :adding  (if update?
                             (update-adding-mode! map-ctx mode)
                             (set-adding-mode! map-ctx mode)))]
    (assoc map-ctx :mode mode)))

(defn map-inner []

  ;; Internal state
  (let [map-ctx* (atom nil)]

    (r/create-class

     {:reagent-render
      (fn [] [mui/grid {:id    "map"
                        :item  true
                        :style {:flex "1 0 0"}
                        :xs    12}])

      :component-did-mount
      (fn [comp]
        (let [opts    (r/props comp)
              basemap (:basemap opts)
              geoms   (:geoms opts)
              mode    (-> opts :mode)

              map-ctx (-> (init-map! opts)
                          (update-geoms! geoms)
                          (set-basemap! basemap)
                          (set-mode! mode))]

          (reset! map-ctx* map-ctx)))

      :component-did-update
      (fn [comp]
        (let [opts    (r/props comp)
              geoms   (-> opts :geoms)
              basemap (-> opts :basemap)
              center  (-> opts :center)
              zoom    (-> opts :zoom)
              mode    (-> opts :mode)]

          (cond-> @map-ctx*
            (not= (:geoms @map-ctx*) geoms)     (update-geoms! geoms)
            (not= (:basemap @map-ctx*) basemap) (set-basemap! basemap)
            (not= (:center @map-ctx*) center)   (update-center! center)
            (not= (:zoom @map-ctx*) zoom)       (update-zoom! zoom)
            (not= (:mode @map-ctx*) mode)       (update-mode! mode)
            true                                (as-> $ (reset! map-ctx* $)))))

      :display-name "map-inner"})))

(defn map-outer []
  (==> [:lipas.ui.sports-sites.events/get-by-type-code 3110])
  (==> [:lipas.ui.sports-sites.events/get-by-type-code 3130])
  (==> [:lipas.ui.sports-sites.events/get-by-type-code 2510])
  (==> [:lipas.ui.sports-sites.events/get-by-type-code 2520])
  (==> [:lipas.ui.sports-sites.events/get-by-type-code 4402])
  (==> [:lipas.ui.sports-sites.events/get-by-type-code 111])
  (let [geoms   (re-frame/subscribe [::subs/geometries])
        basemap (re-frame/subscribe [::subs/basemap])
        center  (re-frame/subscribe [::subs/center])
        zoom    (re-frame/subscribe [::subs/zoom])
        mode    (re-frame/subscribe [::subs/mode])]
    (fn []
      [map-inner
       {:geoms   @geoms
        :basemap @basemap
        :center  @center
        :zoom    @zoom
        :mode    @mode}])))