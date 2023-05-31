function KBeacon() {}

KBeacon.prototype.startScan = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'startScan', []);
}

KBeacon.prototype.stopScanning = function() {
    cordova.exec(null, null, 'cordovaPluginKBeacon', 'stopScanning', []);
}

KBeacon.prototype.checkPermissions = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'checkPermissions', []);
}

KBeacon.prototype.getDiscoveredDevices = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'getDiscoveredDevices', []);
}

/*
KBeacon.prototype.startScaniOS = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'startScaniOS', []);
}

KBeacon.prototype.getDiscoveredDevicesiOS = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'getDiscoveredDevicesiOS', []);
}
*/

// Installation constructor that binds ToastyPlugin to window
KBeacon.install = function() {
    if (!window.plugins) {
        window.plugins = {};
    }
    window.plugins.kbeacon = new KBeacon();
    return window.plugins.kbeacon;
};
cordova.addConstructor(KBeacon.install);
