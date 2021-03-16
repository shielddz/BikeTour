function initialize() {
    var map = L.map('map').setView([48.833, 2.333], 7); // LIGNE 18

    var osmLayer = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', { // LIGNE 20
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    });

    map.addLayer(osmLayer);

    var customIcon = L.icon({
        iconUrl: 'mountain-bike.png',
        //shadowUrl: 'icon-shadow.png',
        iconSize:     [50, 50], // taille de l'icone
        //shadowSize:   [50, 64], // taille de l'ombre
        iconAnchor:   [32, 64], // point de l'icone qui correspondra à la position du marker
        //shadowAnchor: [32, 64],  // idem pour l'ombre
        popupAnchor:  [-3, -76] // point depuis lequel la popup doit s'ouvrir relativement à l'iconAnchor
    });



$.getJSON('https://bicycleinfo0803.herokuapp.com/coordinates', function(data) {
    for (var i=0; i< data.coordinates.length;i++){
        L.marker([data.coordinates[i].lat, data.coordinates[i].lon], {icon: customIcon}).bindPopup(data.coordinates[i].key).openPopup().addTo(map);
    }

    
})
}