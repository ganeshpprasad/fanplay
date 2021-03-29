//
//  SCDModel.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import Foundation

struct SCDModel: Codable {
  let id: Int = 0
  let deviceType:Int  = 0
  let steps: Int  = 0
  let calorie: Int = 0
  let distance: Int = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case deviceType = "deviceType"
    case steps = "steps"
    case calorie = "calorie"
    case distance = "distance"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct DeviceModel: Codable {
  let address: String = ""
  let type:Int  = 0 // BAND or EMOTE
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case address = "address"
    case type = "type"
    case lastSynced = "lastSynced"
  }
}


struct FanDataModel: Codable {
  let id: Int = 0
  let totalTapCount:Int  = 0
  let fanMetric: Float  = 0
  let totalPoints: Double = 0
  let flag: Int = 0
  let playerId: Double  = 0.0
  let teamId: Double  = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case totalTapCount = "totalTapCount"
    case fanMetric = "fanMetric"
    case totalPoints = "totalPoints"
    case flag = "flag"
    case playerId = "playerId"
    case teamId = "teamId"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct HeartRateModel: Codable {
  let id: Int = 0
  let heartRate:Int  = 0
  let type:Int  = 0 // BAND, GOOGLE FIT, CAMERA
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case heartRate = "heartRate"
    case type = "type"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct WaveDataModel: Codable {
  let id: Int = 0
  let waveCount:Int  = 0
  let type:Int  = 0 // PHONE or EMOTE
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case waveCount = "waveCount"
    case type = "type"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct WhistleDataModel: Codable {
  let id: Int = 0
  let whistleCount:Int  = 0
  let whistleEarned:Int  = 0
  let whistleRedeemed:Int  = 0
  let whistleType:Int  = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case whistleCount = "whistleCount"
    case whistleEarned = "whistleEarned"
    case whistleRedeemed = "whistleRedeemed"
    case whistleType = "whistleType"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}


struct TeamModel: Codable {
  let id: Int = 0
  let teamName:String = ""
  let teamIdServer:Double = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case teamName = "teamName"
    case teamIdServer = "teamIdServer"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct PlayersModel: Codable {
  let id: Int = 0
  let teamId:Int = 0
  let playerId:Int = 0
  let playerName:String = ""
  let isPlaying:Bool = false
  let isPlayerActive:Bool = false
  let tapCount:Int = 0
  let waveCount:Int = 0
  let whistleCount:Int = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case teamId = "teamId"
    case playerId = "playerId"
    case playerName = "playerName"
    case isPlaying = "isPlaying"
    case isPlayerActive = "isPlayerActive"
    case tapCount = "tapCount"
    case waveCount = "waveCount"
    case whistleCount = "whistleCount"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct AdvertiserModel: Codable {
  let id: Int = 0
  let imageUrl:String = ""
  let clickUrl:String = ""
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case imageUrl = "imageUrl"
    case clickUrl = "clickUrl"
  }
}

struct UserModel: Codable {
  let id: Int = 0
  let tokenId:String = ""
  let sid:String = ""
  let latitude:String = ""
  let longitude:String = ""
  let timeZone:String = ""
  let age:String = ""
  let loginType:String = ""
  let profileName:String = ""
  let profileImgUrl:String = ""
  let teamPref: String  = ""
  let gender: String  = ""
  let mobile:String = ""
  let email:String = ""
  let dob:String = ""
  let city:String = ""
  let height:String = ""
  let heightMeasure:String = ""
  let weight:String = ""
  let weightMeasure:String = ""
  let deviceId: String  = ""
  let phoneDeviceInfo: String  = ""
  let lastUpdated: String  = ""
  let lastSynced: String  = ""
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case tokenId = "tokenId"
    case sid = "sid"
    case latitude = "latitude"
    case longitude = "longitude"
    case timeZone = "timeZone"
    case age = "age"
    case loginType = "loginType"
    case profileName = "profileName"
    case profileImgUrl = "profileImgUrl"
    case teamPref = "teamPref"
    case gender = "gender"
    case mobile = "mobile"
    case email = "email"
    case dob = "dob"
    case city = "city"
    case height = "height"
    case heightMeasure = "heightMeasure"
    case weightMeasure = "weightMeasure"
    case deviceId = "deviceId"
    case phoneDeviceInfo = "phoneDeviceInfo"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct LeaderBoard: Codable {
  let id: Int = 0
  let rank:String = ""
  let name:String = ""
  let latitude:String = ""
  let longitude:String = ""
  let points:String = ""
  let imgpath:String = ""
  let avguserfanemote:String = ""
  let avguserhr:String = ""
  let highestuserfanemote:String = ""
  let totaltapcount: String  = ""
  let totalwavecount: String  = ""
  let totalwhistleredeemed:String = ""
  let highestcheeredplayer:String = ""
  let lastUpdated: String  = ""
  let lastSynced: String  = ""
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case rank = "tokenId"
    case name = "sid"
    case latitude = "latitude"
    case longitude = "longitude"
    case points = "timeZone"
    case imgpath = "age"
    case avguserfanemote = "loginType"
    case avguserhr = "profileName"
    case highestuserfanemote = "profileImgUrl"
    case totaltapcount = "teamPref"
    case totalwavecount = "gender"
    case totalwhistleredeemed = "mobile"
    case highestcheeredplayer = "email"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct HRModel : Codable {
  let id: Int = 0
  let deviceType:Int  = 0
  let heartrate: Int  = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case deviceType = "deviceType"
    case heartrate = "heartrate"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct BPModel : Codable {
  let id: Int = 0
  let deviceType:Int  = 0
  let systolic: Int  = 0
  let diastolic: Int  = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case deviceType = "deviceType"
    case systolic = "systolic"
    case diastolic = "diastolic"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct SleepModel: Codable {
  let id: Int = 0
  let deviceType:Int  = 0
  let sleepDuration: Int  = 0
  let awake: Int  = 0
  let restless: Int  = 0
  let lastUpdated: Double  = 0.0
  let lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case deviceType = "deviceType"
    case sleepDuration = "sleepDuration"
    case awake = "awake"
    case restless = "restless"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
  }
}

struct ActivityModel: Codable {
  let id: Int = 0
  let activityType:Int  = 0
  let scdJson: String  = ""
  let hrJson:  String  = ""
  let bpJson:  String  = ""
  let started: Double  = 0.0
  let ended: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case activityType = "activityType"
    case scdJson = "scdJson"
    case hrJson = "hrJson"
    case bpJson = "bpJson"
    case started = "started"
    case ended = "ended"
  }
}


struct SponsorModel: Codable {
  let id: Int = 0
  let imageUrl: String  = ""
  let clickUrl:  String  = ""
  let locationId:  String  = ""
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case imageUrl = "imageUrl"
    case clickUrl = "clickUrl"
    case locationId = "locationId"
  }
}

struct SponsorAnalyticsModel: Codable {
  let id: Int = 0
  let locationId: String  = ""
  let noOfClicks:  String  = ""
  let screenTime:  String  = ""
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case locationId = "locationId"
    case noOfClicks = "noOfClicks"
    case screenTime = "screenTime"
  }
}


struct UsageAnalyticsModel: Codable {
  let id: Int = 0
  let type: String  = ""
  let value:  Double  = 0.0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case type = "type"
    case value = "value"
  }
}


struct ConstantsConfigModel: Codable {
  let id: String = ""
  let value:  Double  = 0.0
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case value = "value"
  }
}
