//
//  BTUserDefaults.swift
//  DemoProject
//
//  Created by Sysfore on 30/March/2021.
//

import UIKit

enum Preference : String {
    case SID = "sId"
    case TOKEN = "token"
    case TEAM_ID = "teamId"
    case TEAM_AFFILIATION_ID = "affiliationId"
    case LATITUDE = "latitude"
    case LONGITUDE = "longitude"
}

class BTUserDefaults: NSObject {
    static let shared = BTUserDefaults()
    
    //synchornize
    func synchronise(){
        UserDefaults.standard.synchronize()
    }
    
    //clear contents
    func clearContents(){
        UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
        UserDefaults.standard.synchronize()
    }
}

//MARK- Insert Methods
extension BTUserDefaults {
    
    //inserting the default
    func insertString(key:Preference,value:String){
        UserDefaults.standard.set(value, forKey:key.rawValue)
        UserDefaults.standard.synchronize()
    }
    
    //inserting the default
    func insertInt(key:Preference,value:Int){
        UserDefaults.standard.set(value, forKey:key.rawValue)
        UserDefaults.standard.synchronize()
    }
    
    //inserting the default
    func insertBool(key:Preference,value:Bool){
        UserDefaults.standard.set(value, forKey:key.rawValue)
        UserDefaults.standard.synchronize()
    }
    
}

//MARK- Fetch Methods
extension BTUserDefaults {
    
    //get string data
    func getString(key:Preference)->String{
        if let data = UserDefaults.standard.string(forKey: key.rawValue){
            return data
        }
        return ""
    }
    
    //get interger data
    func getInt(key:Preference)->Int{
        if UserDefaults.standard.integer(forKey: key.rawValue) != 0 {
            return UserDefaults.standard.integer(forKey: key.rawValue)
        }
        return 0
    }
    
    //get BOOl data
    func getBool(key:Preference)->Bool{
        if let data = UserDefaults.standard.bool(forKey: key.rawValue) as Bool?{
            return data
        }
        return false
    }
    
}
 
