@objc(cordovaPluginKBeacon) class cordovaPluginKBeacon : CDVPlugin{
// MARK: Properties
var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
    @objc(add:) func add(_ command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
        let param1 = (command.arguments[0] as? NSObject)?.value(forKey: "param1") as? Int
        let param2 = (command.arguments[0] as? NSObject)?.value(forKey: "param2") as? Int
            if let p1 = param1 , let p2 = param2 {
                if p1 >= 0 && p1 >= 0{
                 let total = String(p1 + p2)
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: total)
                }else {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Something wrong")
                }
            }
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
}