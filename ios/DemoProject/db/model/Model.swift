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
  var address: String = ""
  var type:Int  = 0 // BAND or EMOTE
  var sId:String  = ""
  var lastSynced: Double  = 0
  
  enum CodingKeys: String, CodingKey {
    case address = "address"
    case type = "type"
    case sId = "sId"
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
  var id: Int = 0
  var heartRate:Int  = 0
  var type:Int  = 0 // BAND, GOOGLE FIT, CAMERA
  var lastUpdated: Double  = 0.0
  var lastSynced: Double  = 0
  var sId:String  = ""
  
  enum CodingKeys: String, CodingKey {
    case id = "id"
    case heartRate = "heartRate"
    case type = "type"
    case lastUpdated = "lastUpdated"
    case lastSynced = "lastSynced"
    case sId = "sId"
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
  var id: Int = 0
  var whistleCount:Int  = 0
  var whistleEarned:Int  = 0
  var whistleRedeemed:Int  = 0
  var whistleType:Int  = 0
  var lastUpdated: Double  = 0.0
  var lastSynced: Double  = 0
  
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

struct PendingFE: Codable {
  var id: Int = 0
  var json:String  = ""
  var createdDate:String  = ""
  var synced: Bool = false
}
