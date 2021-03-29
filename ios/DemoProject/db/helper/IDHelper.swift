//
//  IDHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//


import Foundation
import SQLite

//MARK:-Common variables
let id = Expression<Int64>("id")
let deviceType = Expression<Int64>("deviceType")
let steps = Expression<Int64>("steps")
let calorie = Expression<Int64>("calorie")
let distance = Expression<Int64>("distance")
let lastUpdated = Expression<Double>("lastUpdated")
let lastSynced = Expression<Double>("lastSynced")

let address = Expression<String>("address")
let type = Expression<Double>("type")

let totalTapCount = Expression<Int64>("totalTapCount")
let fanMetric = Expression<Double>("fanMetric")
let totalPoints = Expression<Double>("totalPoints")
let flag = Expression<Int64>("flag")
let playerId = Expression<Double>("playerId")
let teamId = Expression<Double>("teamId")
 
let heartRate = Expression<Int64>("heartRate")

let waveCount = Expression<Int64>("waveCount")

let whistleCount = Expression<Int64>("whistleCount")
let whistleEarned = Expression<Int64>("whistleEarned")
let whistleRedeemed = Expression<Int64>("whistleRedeemed")
let whistleType = Expression<Int64>("whistleType")

let teamName = Expression<String>("teamName")
let teamIdServer = Expression<Double>("teamIdServer")


let playerName = Expression<String>("playerName")
let isPlaying = Expression<Bool>("isPlaying")
let isPlayerActive = Expression<Bool>("isPlayerActive")
let tapCount = Expression<Int64>("tapCount")

let imageUrl = Expression<String>("imageUrl")
let clickUrl = Expression<String>("clickUrl")

let tokenId = Expression<String>("tokenId")
let sid = Expression<String>("sid")
let latitude = Expression<String>("latitude")
let longitude = Expression<String>("longitude")
let timeZone = Expression<String>("timeZone")
let age = Expression<String>("age")
let loginType = Expression<String>("loginType")
let profileName = Expression<String>("profileName")
let profileImgUrl = Expression<String>("profileImgUrl")
let teamPref = Expression<String>("teamPref")
let gender = Expression<String>("gender")
let mobile = Expression<String>("mobile")
let email = Expression<String>("email")
let dob = Expression<String>("dob")
let city = Expression<String>("city")
let height = Expression<String>("height")
let heightMeasure = Expression<String>("heightMeasure")
let weight = Expression<String>("weight")
let weightMeasure = Expression<String>("weightMeasure")
let deviceId = Expression<String>("deviceId")
let phoneDeviceInfo = Expression<String>("phoneDeviceInfo")

let rank = Expression<String>("rank")
let name = Expression<String>("name")
let points = Expression<String>("points")
let imgpath = Expression<String>("imgpath")
let avguserfanemote = Expression<String>("avguserfanemote")
let avguserhr = Expression<String>("avguserhr")
let highestuserfanemote = Expression<String>("highestuserfanemote")
let totaltapcount = Expression<String>("totaltapcount")
let totalwavecount = Expression<String>("totalwavecount")
let totalwhistleredeemed = Expression<String>("totalwhistleredeemed")
let highestcheeredplayer = Expression<String>("highestcheeredplayer")


let heartrate = Expression<Int64>("heartrate")

let systolic = Expression<Int64>("systolic")
let diastolic = Expression<Int64>("diastolic")

let sleepDuration = Expression<Int64>("sleepDuration")
let awake = Expression<Int64>("awake")
let restless = Expression<Int64>("restless")

let activityType = Expression<Int64>("activityType")
let scdJson = Expression<String>("scdJson")
let hrJson = Expression<String>("hrJson")
let bpJson = Expression<String>("bpJson")
let started = Expression<Double>("started")
let ended = Expression<Double>("ended")
 
let locationId = Expression<String>("locationId")

let noOfClicks = Expression<String>("noOfClicks")
let screenTime = Expression<String>("screenTime")

let value = Expression<Double>("value")

let idStr = Expression<String>("id")
let typeStr = Expression<String>("type")
