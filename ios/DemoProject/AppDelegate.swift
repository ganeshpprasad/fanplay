//
//  AppDelegate.swift
//  DemoProject
//
//  Created by Sysfore on 26/March/2021.
//

import Foundation
import UIKit
import React

#if FB_SONARKIT_ENABLED
import FlipperKit
private func InitializeFlipper(_ application: UIApplication?) {
  let client = FlipperClient.shared()
  let layoutDescriptorMapper = SKDescriptorMapper()
  client?.add(FlipperKitLayoutPlugin(rootNode: application, with: layoutDescriptorMapper))
  client?.add(FKUserDefaultsPlugin(suiteName: nil))
  client.add(FlipperKitReactPlugin())
  client.add(FlipperKitNetworkPlugin(networkAdapter: SKIOSNetworkAdapter()))
  client.start()
}
#endif

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate , RCTBridgeDelegate {
  
  var window: UIWindow?
  var bridge: RCTBridge!
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    #if FB_SONARKIT_ENABLED
    InitializeFlipper(application);
    #endif
    
    UIApplication.shared.setMinimumBackgroundFetchInterval(30)
    
    //DB Initialization
    do {
        if let database = DTDatabase() {
            try database.createTables()
            try database.migrateIfNeeded()
            AppConstants.log(database)
        }else{
            fatalError("could not setup database")
        }
    } catch {
        fatalError("failed to migrate database: \(error)")
    }
    
    //RTC bridge view
    bridge = RCTBridge(delegate: self, launchOptions: launchOptions)
    let rootView = RCTRootView(
      bridge: bridge,
      moduleName: "DemoProject",
      initialProperties: nil)
    if #available(iOS 13.0, *) {
      rootView.backgroundColor = UIColor.systemBackground
    } else {
      rootView.backgroundColor = UIColor.white
    }
    
    window = UIWindow(frame: UIScreen.main.bounds)
    let rootViewController = UIViewController()
    rootViewController.view = rootView
    window?.rootViewController = rootViewController
    window?.makeKeyAndVisible()
    return true
  }
  
  //set the source url
  func sourceURL(for bridge: RCTBridge!) -> URL! {
    #if DEBUG
    return RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index", fallbackResource: nil)
    #else
    return Bundle.main.url(forResource: "main", withExtension: "jsbundle")
    #endif
  }
  
  //go to camera pulse view controller
  func goToPulseViewController(pulseViewDelegate:PulseViewDelegate) {
    DispatchQueue.main.async {
      let customViewController = PulseViewController.init(pulseViewDelegate: pulseViewDelegate)
      if let controller = self.window?.rootViewController {
        controller.present(customViewController, animated: true, completion: nil)
      }else{
        print("not Found contoller rootViewController")
        pulseViewDelegate.onHeartRateReceved(0)
      } 
    }
  }
  
  
}
