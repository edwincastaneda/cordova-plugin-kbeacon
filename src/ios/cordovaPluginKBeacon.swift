import Foundation

import kbeaconlib2

@objc(cordovaPluginKBeacon)

public class cordovaPluginKBeacon : CDVPlugin, KBeaconMgrDelegate{

    var mBeaconsMgr: KBeaconsMgr?
    static var mBeaconsDictory = [String:Any]()
    static var mBeaconsArray = [Any]()

    public override func pluginInitialize() {
        super.pluginInitialize()
        print("Plugin inicializado")
        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager
        mBeaconsMgr!.delegate = self
    }

    public func onCentralBleStateChange(newState: kbeaconlib2.BLECentralMgrState) {
        if (newState == BLECentralMgrState.PowerOn)
        {
            //the app can start scan in this case
            print("central ble state power on")
        }
    }

    public func onBeaconDiscovered(beacons: [kbeaconlib2.KBeacon]) {

        for beacon in beacons
        {
            printScanPacket(beacon)
        }

        if cordovaPluginKBeacon.mBeaconsDictory.count > 0 {
            cordovaPluginKBeacon.mBeaconsArray = Array(cordovaPluginKBeacon.mBeaconsDictory.values)
        }


    }

    func printScanPacket(_ advBeacon: KBeacon){

        guard let allAdvPackets = advBeacon.allAdvPackets else{
            return
        }
        for advPacket in allAdvPackets
        {
            switch advPacket.getAdvType()
            {
            case KBAdvType.IBeacon:

                if let iBeaconAdv = advPacket as? KBAdvPacketIBeacon{

                    var KBArray = [Any]()

                    KBArray.append(String(advBeacon.rssi))
                    KBArray.append(String(iBeaconAdv.refTxPower))
                    KBArray.append(advBeacon.uuidString!)
                    KBArray.append(String(iBeaconAdv.minorID))
                    KBArray.append(String(iBeaconAdv.majorID))

                    cordovaPluginKBeacon.mBeaconsDictory[String(advBeacon.minorID)!] = KBArray
                }

            default:
                print("unknown packet")
            }
        }

        //remove buffered packet
        advBeacon.removeAdvPacket()
    }

    @objc
    func getDiscoveredDevices(_ command: CDVInvokedUrlCommand){

        let pluginResult:CDVPluginResult
        if cordovaPluginKBeacon.mBeaconsDictory.count > 0 {
            pluginResult = CDVPluginResult.init(status: CDVCommandStatus_OK, messageAs: cordovaPluginKBeacon.mBeaconsArray)
        } else {
            pluginResult = CDVPluginResult.init(status: CDVCommandStatus_ERROR)
        }



        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)

    }

    @objc
    func startScan(_ command: CDVInvokedUrlCommand) {
        let pluginResult:CDVPluginResult
        let scanResult = mBeaconsMgr!.startScanning()

          if (scanResult){
              print("Iniciando la lectura de dispositivos")
              pluginResult = CDVPluginResult.init(status: CDVCommandStatus_OK, messageAs: "Iniciando la lectura de dispositivos")
          }
          else{
              print("Error en la lectura de dispositivos")
              pluginResult = CDVPluginResult.init(status: CDVCommandStatus_ERROR)
          }

        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
      }

    @objc
    func stopScanning() {
         mBeaconsMgr!.stopScanning()
      }

}
