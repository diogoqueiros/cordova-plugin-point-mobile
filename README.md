# Cordova Plugin Point Mobile
Cordova barcode scanning plugin for Point Mobile devices

## Supported Platforms

- Android

## Installation

```bash
cordova plugin add cordova-plugin-point-mobile
```

## Usage

### scan

Bind the device callback event. Each call of the scan function will remove the old callback.

```js
const pointMobile = window.cordova.plugins.pointmobile;

pointMobile.scan(
    data => {
      console.log('## Point Mobile barcode received -> ', data);
      console.log('barcode: ', data.barcode);
      console.log('barcode type: ', data.type);
    },
    error => {
      console.log('## Point Mobile error -> ', error);
    },
);
```

### cancel

Unbind the current callback function returned by scan function.

```js
pointMobile.cancel();
```
