// var exec = require('cordova/exec');

function BluetoothFileTransfer() {}

BluetoothFileTransfer.prototype.sendFile = function () {
	var arg0={};
    return (new Promise(function(resolve,reject){
        cordova.exec(function(str) {
          var json = JSON.parse(str);
          resolve(json);
        }, function(err) {
          reject(err);
        }, 'BluetoothFileTransfer', 'sendFile', [arg0]);
	}));
};

BluetoothFileTransfer.prototype.sendObject = function (options) {
    return (new Promise(function(resolve,reject){
        cordova.exec(function(str) {
          var json = JSON.parse(str);
          resolve(json);
        }, function(err) {
          reject(err);
        }, 'BluetoothFileTransfer', 'sendObject', [options]);
	}));
};

// Installation constructor that binds NFCPlugin to window
BluetoothFileTransfer.install = function() {
  if (!cordova.plugins) {
    cordova.plugins = {};
  }
  cordova.plugins.BluetoothFileTransfer = new BluetoothFileTransfer();
  return cordova.plugins.BluetoothFileTransfer;
};
cordova.addConstructor(BluetoothFileTransfer.install);
