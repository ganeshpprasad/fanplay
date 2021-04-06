//
//  ApiResponseModel.swift
//  DemoProject
//
//  Created by Sysfore on 29/March/2021.
//

import UIKit 

// MARK: - ValidateSignIn
struct ValidateSignIn: Codable {
  let response: ValidateSignInResponse?
  let statuscode: Int
  let message, callstarttime, callendtime: String
}

// MARK: - ValidateSignInResponse
struct ValidateSignInResponse: Codable {
  let userSignUpID: Int
  
  enum CodingKeys: String, CodingKey {
    case userSignUpID = "userSignUpId"
  }
}


// MARK: - UserDetailsResponse
struct UserDetailsResponse: Codable {
  var response: UserDetailsDataResponse?
  let statuscode: Int
  let message, callstarttime, callendtime: String
}

// MARK: - UserDetailsDataResponse
struct UserDetailsDataResponse: Codable {
  var result: [UserDetails]?
}

// MARK: - UserDetails
struct UserDetails: Codable {
  var sid: Int?
  var token: String?
  var latitude: String?
  var longitude: String?
  var email, displayname, rolename: String?
  var teamid: Int?
  var favouriteteam, gender: String?
  var profileimage: String?
  var mobilenumber: String?
  var dob: String?
  var loginType:Int?
  var ageinyears: Int?
  var city, state, country, countrycode: String?
  var height, heightmeasure, weight, weightmeasure: String?
  var devicemacid, phonedeviceinfo, bloodsugar, bloodpressure: String?
  var heartrate, steps, calories, distance: Int?
  var sleepinghours, totalpoints: Int?
  var fescore: Double?
  var hrcount, tapcounts, wavecounts, whistlesredeemed: Int?
  var whistlecounts, cheeredteamid: Int?
  var affiliationname: String?
  var affiliationid: Int?
  var affiliationlogo: String?
  var active, doyoufollowanyfitnessregimen, interestedincustomizedfitnessregimen: Bool?
  var physicalactivities: [PhysicalActivityDetails]?
  var habits: [HabitDetails]?
  var healthissues: [HealthDetails]?
  var sports: [SportDetails]?
}

// MARK: - PhysicalActivityDetails
struct PhysicalActivityDetails: Codable {
  var activityid: Int?
  var activityname: String?
  var activitylogo: String?
  var perdayhour, perdaymin, perweekhour, perweekmin: Int?
}

// MARK: - SportDetails
struct SportDetails: Codable {
  var sportid: Int?
  var sportname: String?
  var sportslogo: String?
}

// MARK: - HabitDetails
struct HabitDetails: Codable {
  var habitid: Int?
  var habitname: String?
  var habitlogo: String?
}

// MARK: - HealthDetails
struct HealthDetails: Codable {
  var healthid: Int?
  var healthname: String?
  var healthlogo: String?
}


// MARK: - PlayerData
struct TeamPlayersData: Codable {
  let response: TeamResponse?
  let statuscode: Int
  let message, callstarttime, callendtime: String
}

// MARK: - PlayerDataResponse
struct TeamResponse: Codable {
  let playersdata: [TeamData]?
}

// MARK: - Playersdatum
struct TeamData: Codable {
  var teamid: Int?
  var teamname, sportname, tournamentname: String?
  var teamlogourl: String?
  var teamstoreurl: String?
  var affiliationid: String?
  var players: [PlayerData]?
}

// MARK: - Player
struct PlayerData: Codable {
  var playerid: Int?
  var playername: String?
  var playerimagepath: String?
  var isplaying, isplayeractive: Bool?
  var playeradditionalinfo: String?
  var tapCount:Int? = 0
  var waveCount:Int? = 0
  var whistleCount:Int? = 0
}

// MARK: - Default Response
struct DefaultResponse: Codable {
  let statuscode: Int
  let message, callstarttime, callendtime: String
}


// MARK: - Fe Detaild
struct FeDetailsResult: Codable {
    let response: FeDetailsResponse
    let statuscode: Int
    let message, callstarttime, callendtime: String
}

// MARK: - Response
struct FeDetailsResponse: Codable {
    let result: [FeDetails]
}

// MARK: - Result
struct FeDetails: Codable {
    let tapcounts, wavecounts, whistlesredeemed, whistlecounts: Int
    let fescore: Double
    let points: Int
}
