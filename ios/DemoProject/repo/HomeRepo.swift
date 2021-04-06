//
//  HomeRepo.swift
//  DemoProject
//
//  Created by Sysfore on 30/March/2021.
//

import UIKit
import Alamofire
import CoreLocation

class HomeRepo: NSObject {
  
  let locationService = LocationService()
  
  var headers: HTTPHeaders = [
    "Authorization": "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjhkOGM3OTdlMDQ5YWFkZWViOWM5M2RiZGU3ZDAwMzJmNjk3NjYwYmQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZmFucGxheS1kZXYiLCJhdWQiOiJmYW5wbGF5LWRldiIsImF1dGhfdGltZSI6MTYxNzY4MzcxMywidXNlcl9pZCI6IjlDazNKSUVaSnZaMjhHN3BTb1NxT1dUQzhubjIiLCJzdWIiOiI5Q2szSklFWkp2WjI4RzdwU29TcU9XVEM4bm4yIiwiaWF0IjoxNjE3NjgzNzEzLCJleHAiOjE2MTc2ODczMTMsImVtYWlsIjoiZW1tYUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsiZW1tYUBnbWFpbC5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.P61sDOwXKOtpRfOPloOXmtFFbqtQIXGieXsrsWUvfv0nIdPrTMBL74nLtVhQIz3bLatIFJfNb_RG8mCmkpxFQyGXYDuV2dxCSraVxN3MOPGTLcE7pPv1eDWk8aZJ42v56LviqyoUKHH5XVVWcotwtnSB1v99nIQmZmMxZmQ0b5zTYQj_6PLoMNe09wXkjbs_v1r6_jxMplxewH2gEr4Yw_ew7pyOX2O1adVugyole6CiaDJanTehy3DGOxffRw-R6dzXVb2Di0H_YU_cQnl8Pt3OvRJfiffJEzbybW75fRnCnLs-LOATSAbaWQuYvKFHq14zQ205S82fV6OCuqvxuw",
    "Accept": "application/json"
  ]
  
  private var isConnectedToInternet:Bool {
    return NetworkReachabilityManager()?.isReachable ?? false
  }
}

//MARK:- db methods
extension HomeRepo {
  public func insertTeam(_ teamIdServer: Int, callBack: @escaping (Bool) -> Void){
    self.getTeamPlayersData(String(teamIdServer)) { (result) in
      if let res = result {
        print(res)
      }else{
        callBack(false)
      }
    }
  }
  
  public func incrementWhistle(_ count: Int, playerId: Int) {
    let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    DispatchQueue.main.async {
      MP3Player.shared.playLocalFile(name: "whistle")
      print(try! WhistleDataHelper.updateWhistleCount(count: count))
      print(try! PlayersHelper.updateWhistleCount(count: count, teamId: teamId, playrId: playerId))
    }
  }
  
  public func updateWaveCount(_ count: Int, playerId: Int) {
    DispatchQueue.main.async {
      let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
      print(try! WaveDatahelper.updateWaveCount(count: count))
      print(try! PlayersHelper.updateWaveCount(count: count, teamId: teamId, playrId: playerId))
    }
  }
  
  public func updateTapCount(_ count: Int, playerId: Int) {
    DispatchQueue.main.async {
      let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
      print(try! FanDataHelper.updateTapCount(count: count))
      print(try! PlayersHelper.updateTapCount(count: count, teamId: teamId, playrId: playerId))
    }
  }
}
//MARK:- Api Calls
extension HomeRepo {
  
  public func insertOrUpdateUser(tokenId: String, tokenExpires: Double, displayName: String?, type: Double, callBack: @escaping (Bool) -> Void) {
    //get the user id
    let userId = BTUserDefaults.shared.getString(key: .SID)
    
    //get the data
    let data = UserHelper.getCurrentUser(sId: userId)
    
    // check data count
    if data.count == 0 {
      // Download the user data
      self.getAllUserDetailsByIdToken(displayName!, type: type){ (result) in
        if let res = result {
          print(res)
        }else{
          callBack(false)
        }
      }
    }else if let u = data.first {
      var usr = u
      usr.token = tokenId
      usr.displayname = displayName
      usr.loginType = Int(type)
      let status = try! UserHelper.updateToken(userDetail: usr)
      callBack(status)
    }else{
      callBack(false)
    }
  }
  
  public func getAllUserDetailsByIdToken(_  displayName: String, type: Double, callback: @escaping (RCTResponseSenderBlock)){
    DispatchQueue.main.async {
      if(!self.isConnectedToInternet){
        callback(["Internet Not Connected"])
      }
      
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      
      AF.request(BASE_URL + USER_DETAIL_BY_ID_TOKEN, method: .get,  encoding: JSONEncoding.default, headers: self.headers).responseJSON { (response) in
        AppConstants.log(response)
        switch (response.result) {
        case .success( _):
          do {
            let userDetailsResponse = try JSONDecoder().decode(UserDetailsResponse.self, from: response.data!)
            if userDetailsResponse.statuscode == 200 , let userDetails = userDetailsResponse.response?.result{
              userDetails.forEach { (u) in
                BTUserDefaults.shared.insertString(key: .SID, value: String(u.sid!))
                var user = u
                user.displayname = displayName
                user.loginType = Int(type)
                user.token = token
                user.latitude = BTUserDefaults.shared.getString(key: .LATITUDE)
                user.longitude = BTUserDefaults.shared.getString(key: .LONGITUDE)
                print(try! UserHelper.insertUserDetails(userDetails: user))
              }
              callback([true])
            }else{
              print("Failed to load: \(response.data!)")
              callback([false])
            }
          } catch let error as NSError {
            print("Failed to load: \(error.localizedDescription)")
            callback([error.localizedDescription])
          }
        case .failure(let error):
          print("Request error: \(error.localizedDescription)")
          callback([error.localizedDescription])
        }
      }
    }
  }
  
  public func getTeamPlayersData(_ teamId:String, callback: @escaping (RCTResponseSenderBlock)){
    DispatchQueue.main.async {
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      let userId = BTUserDefaults.shared.getString(key: .SID)
      if(!self.isConnectedToInternet){
        callback(["Internet Not Connected"])
      }
      AF.request(BASE_URL + PLAYERS_DATA + "?teamId=\(teamId)", method: .get,  encoding: URLEncoding.queryString, headers: self.headers).responseJSON { (response) in
        AppConstants.log(response)
        switch (response.result) {
        case .success( _):
          do {
            let teamPlayersData = try JSONDecoder().decode(TeamPlayersData.self, from: response.data!)
            if teamPlayersData.statuscode == 200 , let playerData = teamPlayersData.response?.playersdata{
              playerData.forEach { (team) in
                do {
                  if let affiliationid = team.affiliationid {
                    BTUserDefaults.shared.insertString(key: .TEAM_AFFILIATION_ID, value: affiliationid)
                  }else{
                    BTUserDefaults.shared.insertString(key: .TEAM_AFFILIATION_ID, value: "0")
                  }
                  let r = try TeamHelper.insert(userId: userId, item: team)
                  print(r)
                } catch let err {
                  print(err)
                }
                
                if let players = team.players {
                  players.forEach { (play) in
                    do {
                      let r = try PlayersHelper.insert(userId: userId,tId: String(team.teamid!), item: play)
                      print(r)
                    } catch let err {
                      print(err)
                    }
                  }
                }
              }
              callback(["Team data inserted to database"])
            }else{
              print("Failed to load: \(response.data!)")
              callback([response.data!])
            }
          } catch let error as NSError {
            print(error)
            print("Failed to load: \(error.localizedDescription)")
            callback([error.localizedDescription])
          }
          callback([response.data!])
        case .failure(let error):
          print(error)
          print("Request error: \(error.localizedDescription)")
          callback([error.localizedDescription])
        }
      }
    }
  }
  //    Token ID to be feteched from db
  public func validateSignIn(_ callback: @escaping (RCTResponseSenderBlock)){
    DispatchQueue.main.async {
      //    role id = 3 hard coded
      //    display name is not required
      //    lat & long to be captured from device
      //       get the sid
      //       sid is the user id in the data base
      //       sid to be sent in the regarding api
      
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      
      if(!self.isConnectedToInternet){
        callback(["Internet Not Connected"])
      }
      Locator.shared.locate { (loc, err) in
        if let err = err {
          callback([err.localizedDescription])
        }else if let loc = loc {
          let parameters: [String : Any] = [
            "roleid": 3,
            "displayname": "Emma",
            "latitiude": loc.location?.coordinate.latitude ?? 0.0,
            "longitude":  loc.location?.coordinate.longitude ?? 0.0
          ]
          BTUserDefaults.shared.insertString(key: .LATITUDE, value: String(loc.location?.coordinate.latitude ?? 0.0))
          BTUserDefaults.shared.insertString(key: .LONGITUDE, value: String(loc.location?.coordinate.longitude ?? 0.0))
          
          AF.request(BASE_URL + VALIDATE_LOGIN, method: .post, parameters: parameters, encoding: JSONEncoding.default,headers: self.headers).responseJSON { (response) in
            switch (response.result) {
            case .success( _):
              do {
                let validateSignId = try JSONDecoder().decode(ValidateSignIn.self, from: response.data!)
                print(validateSignId)
                if validateSignId.statuscode == 200 {
                  BTUserDefaults.shared.insertString(key: .SID, value: String(validateSignId.response!.userSignUpID))
                  callback([true])
                }else{
                  print("Failed to load: \(response.data!)")
                  callback([false])
                }
              } catch let error as NSError {
                print("Failed to load: \(error.localizedDescription)")
                callback([error.localizedDescription])
              }
            case .failure(let error):
              print("Request error: \(error.localizedDescription)")
              callback([error.localizedDescription])
            }
          }
        }
      }
    }
  }
  
  public func getFanEmote(_ teamId:String,callback: @escaping (RCTResponseSenderBlock)){
    DispatchQueue.main.async {
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      if(!self.isConnectedToInternet){
        callback(["Internet Not Connected"])
      }
      //Duration to be hard coded or added from the configuration from the api
      AF.request(BASE_URL + FAN_EMOTE + "?teamId=\(teamId)&duration=8600", method: .get,  encoding: URLEncoding.queryString, headers: self.headers).responseJSON { (response) in
        AppConstants.log(response)
        switch (response.result) {
        case .success( _):
          if let json = try? JSONSerialization.jsonObject(with: response.data!, options: []) as? [String: Any] {
            callback([json])
          }else{
            callback([response.data!])
          }
        case .failure(let error):
          print("Request error: \(error.localizedDescription)")
          callback([error.localizedDescription])
        }
      }
    }
  }
  
  public func getFEDetailsByTeamId(_ teamId:String,callback: @escaping (RCTResponseSenderBlock)){
    DispatchQueue.main.async {
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      if(!self.isConnectedToInternet){
        callback(["Internet Not Connected"])
      }
      AF.request(BASE_URL + FE_DETAILS_BY_TEAM_ID + "?teamId=\(teamId)", method: .get,  encoding: URLEncoding.queryString, headers: self.headers).responseJSON { (response) in
        AppConstants.log(response)
        switch (response.result) {
        case .success( _):
          if let json = try? JSONSerialization.jsonObject(with: response.data!, options: []) as? [String: Any] {
            callback([json])
          }else{
            callback([response.data!])
          }
        case .failure(let error):
          print("Request error: \(error.localizedDescription)")
          callback([error.localizedDescription])
        }
      }
    }
  }
  
  
  public func callCreateFanEngageMentApi(heartRateModel:HeartRateModel){
    //Calling callCreateFanEngageMentApi
    print("callCreateFanEngageMentApi") 
    locationService.newLocation = { result in
      switch result {
      case .success(let location):
        print(location.coordinate.latitude, location.coordinate.longitude)
        self.sendDataToServer(location: location, heartRateModel: heartRateModel)
      case .failure(let error):
        print(error)
        print("Location not found")
      }
    }
//    DispatchQueue.main.async {
//      Locator.shared.locate { (loc, err) in
//        if let err = err {
//          print(err)
//        }else if let loc = loc {
//
//
//        }else{
//          print("Location not found")
//        }
//      }
//    }
  }
  
  private func sendDataToServer(location:CLLocation, heartRateModel:HeartRateModel){
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    var deviceMacAddress = ""
    if MCBLESDK.connectionStateIsConnected() {
      deviceMacAddress = MCBLESDK.currentDevice()?.macAddress ?? "02:00:00:00:00:00"
    }else{
      deviceMacAddress = "02:00:00:00:00:00"
    }
    
    let playersData = PlayersHelper.getAllPlayers(tId: teamId)
    
    var playertapcheer:[[String:Any]] = []
    var playerwavecheer:[[String:Any]] = []
    var playerwhistleredeemed:[[String:Any]] = []
    
    playersData.forEach { (playerData) in
      if let tapCount = playerData.tapCount, let pId = playerData.playerid {
        playertapcheer.append(["playerid":pId , "tapvalue":tapCount])
      }
      
      if let whistleCount = playerData.whistleCount , let pId = playerData.playerid {
        playerwhistleredeemed.append(["playerid":pId , "wrvalue":whistleCount])
      }
      
      if let waveCount = playerData.waveCount, let pId = playerData.playerid {
        playerwavecheer.append(["playerid": pId, "wavevalue":waveCount])
      }
    }
    
    let parameters: [String : Any] =     [
      "sid": heartRateModel.sId,
      "hrcount": heartRateModel.heartRate,
      "hrdevicetype": heartRateModel.type,
      "datacollectedts": AppConstants.getFormattedCurrentDate(),
      "teamcheered": teamId,
      "tapcounts": FanDataHelper.getCurrentCount(userId: userId, teamId: teamId),
      "wavecounts": WaveDatahelper.getCurrentCount(userId: userId, teamId: teamId),
      "whistlesredeemed": WhistleDataHelper.getCurrentCount(userId: userId, teamId: teamId, position: 2),
      "whistlecounts": WhistleDataHelper.getCurrentCount(userId: userId, teamId: teamId, position: 0),
      "fescore": 0,
      "points": 0,
      "latitiude": location.coordinate.latitude,
      "longitude":  location.coordinate.longitude,
      "devicemacid": deviceMacAddress,
      "hrzoneid": 0,
      "affiliationid": BTUserDefaults.shared.getString(key: .TEAM_AFFILIATION_ID),
      "playertapcheer": playertapcheer,
      "playerwavecheer": playerwavecheer,
      "playerwhistleredeemed": playerwhistleredeemed
    ]
    
    var insertRowId = -1
    if let jsonData = try? JSONSerialization.data(withJSONObject: parameters, options: []),
       let jsonString = String(data: jsonData, encoding: String.Encoding.utf8) {
      var item = PendingFE.init()
      item.synced = false
      item.json = jsonString
      if let s = try? PendingFEHelper.insert(item: item) {
        insertRowId = s
        print("Data Inserted PendingFEHelper:\(s)")
      }
    }
    self.createFanEngagement(insertRowId, parameter: parameters) { (rowId, result) in
      print(result)
      if let data = result as? Data {
        do {
          let defaultResponse = try JSONDecoder().decode(DefaultResponse.self, from: data)
          if defaultResponse.statuscode == 200 {
            // make all count to zero
            print("Status updated: \(try! PlayersHelper.updateTapWaveWhistleCountToZero())")
            print("Status updated: \(try! PendingFEHelper.updateSyncedTrue(itemId: rowId))")
            self.callGetFeApi()
          }else{
            //failed
            print(defaultResponse.message)
          }
        } catch let error as NSError {
          print("Failed to load: \(error.localizedDescription)")
          //failed
          print("Did Not Sync the data to sever")
        }
      }else if let data = result as? String {
        // Failed
        print(data)
      }else{
        print("Did Not Sync the data to sever")
      }
    }
  }
  
  //call to get fe details
  private func callGetFeApi(){
    DispatchQueue.main.async {
      let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
      self.getFEDetailsByTeamId(String(teamId)) { (result) in
        if let data = result {
          if let dataArray = data[0] as? [String:Any] {
            //check the result and update the count
            if let jsonData = try? JSONSerialization.data(withJSONObject: dataArray, options: []){
              do {
                let feDetailsResult = try JSONDecoder().decode(FeDetailsResult.self, from: jsonData)
                if feDetailsResult.statuscode == 200  {
                  let feDetails = feDetailsResult.response.result
                  feDetails.forEach { (feDet) in
                    if let s = try? WhistleDataHelper.updateWhistleEarnedCountFromFe(feDetails: feDet) {
                      print("Whistle earned count:\(s)")
                    }else{
                      print("Whistle earned count:\(false)")
                    }
                  }
                }else{
                  print("ERROR:", feDetailsResult.message)
                }
              } catch {
                print("ERROR:", error)
              }
            }
          }else if let dataResult = data[0] as? String {
            print(dataResult)
          }
        }
      }
    }
  }
  
  public func callPendingFanEngagemnetSyncApi(){
    let dataList = PendingFEHelper.getAllPendingFe()
    if dataList .count > 0 {
      var dataParams: [[String : Any]] = []
      dataList.forEach { (pendin) in
        if let data = pendin.json.convertJsonStringToDictionary() {
          dataParams.append(data)
        }
      }
      DispatchQueue.main.async {
        self.syncFanEngagement(dataParams) { (result) in
          print(result)
          if let data = result as? Data {
            do {
              let defaultResponse = try JSONDecoder().decode(DefaultResponse.self, from: data)
              if defaultResponse.statuscode == 200 {
                // make all count to zero
                dataList.forEach { (pe) in
                  print("Status updated: \(try! PendingFEHelper.updateSyncedTrue(itemId: pe.id))")
                }
              }else{
                //failed
                print(defaultResponse.message)
              }
            } catch let error as NSError {
              print("Failed to load: \(error.localizedDescription)")
              //failed
              print("Did Not Sync the data to sever")
            }
          }else if let data = result as? String {
            // Failed
            print(data)
          }else{
            print("Did Not Sync the data to sever")
          }
        }
      }
      
    }
  }
  
  private func createFanEngagement(_ rowId:Int, parameter:[String : Any], callback: @escaping ((Int,Any)->Void)){
    DispatchQueue.main.async {
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      if(!self.isConnectedToInternet){
        callback(rowId,"Internet Not Connected")
      }
      AF.request(BASE_URL + CREATE_FAN_ENGAGEMENT, method: .post, parameters: parameter, encoding: JSONEncoding.default, headers: self.headers).responseJSON { (response) in
        AppConstants.log(response)
        switch (response.result) {
        case .success( _):
          callback(rowId,response.data!)
        case .failure(let error):
          print("Request error: \(error.localizedDescription)")
          callback(rowId,error.localizedDescription)
        }
      }
    }
  }
  
  private  func syncFanEngagement(_ parameters: [[String : Any]], callback: @escaping ((Any)->Void)){
    DispatchQueue.main.async {
      let token = BTUserDefaults.shared.getString(key: .TOKEN)
      if token.count > 0 {
        self.headers = [
          "Authorization": "Bearer \(token)",
          "Accept": "application/json"
        ]
      }
      
      if(!self.isConnectedToInternet){
        callback("Internet Not Connected")
      }
      
      if let url = URL.init(string: BASE_URL + SYNC_FAN_ENGAGEMENT) {
        let request = NSMutableURLRequest(url: url)
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpMethod = HTTPMethod.post.rawValue
        request.httpBody = try! JSONSerialization.data(withJSONObject: parameters, options: [])
        
        AF.request(request as! URLRequestConvertible).responseJSON { (response) in
          AppConstants.log(response)
          switch (response.result) {
          case .success( _):
            callback(response.data!)
          case .failure(let error):
            print("Request error: \(error.localizedDescription)")
            callback(error.localizedDescription)
          }
        }
      }else{
        callback("Failed url parsing")
      }
    }
  }
}
