//
//  PedoMeterVC.swift
//  DemoProject
//
//  Created by Sysfore on 31/March/2021.
//

import UIKit
import CoreMotion

class PedoMeterVC: UIViewController, PIPUsable {
  
  var initialState: PIPState { return .pip }
  
  var initialPosition: PIPPosition { return .bottomRight }
  
  var pipShadow: PIPShadow? = nil
  var pipCorner: PIPCorner? = nil
  
  var recordStarted:Bool = false {
    didSet{
      self.toggleButton()
    }
  }
  
//  var distance:Double = 0
//  var averagePace:Double = 0
//  var pace:Double = 0
  var numberOfSteps:Int = 0
  
  //the pedometer
  var pedometer = CMPedometer()
  
  // timers
  var timer = Timer()
  var timerInterval = 1.0
  var timeElapsed:TimeInterval = 1.0
  
  
  @IBOutlet weak var lbTitle: UILabel!
  @IBOutlet weak var lbActivityState: UILabel!
  @IBOutlet weak var lbStepsCount: UILabel!
  @IBOutlet weak var lbTimer: UILabel!
  @IBOutlet weak var btRecord: UIButton!
  @IBOutlet weak var btStop: UIButton!
  @IBOutlet weak var uvCard: UIView!
  
  let activityManager = CMMotionActivityManager()
  var pedoMeter = CMPedometer()
  
  public init() {
    super.init(nibName: "PedoMeterVC", bundle: Bundle(for: PedoMeterVC.self))
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    self.setView()
    
    if(CMMotionActivityManager.isActivityAvailable()){
      self.activityManager.startActivityUpdates(to: OperationQueue.main) { (data) in
        DispatchQueue.main.async {
          if let data = data {
            if(data.stationary == true){
              self.lbActivityState.text = "Stationary"
            } else if (data.walking == true){
              self.lbActivityState.text = "Walking"
            } else if (data.running == true){
              self.lbActivityState.text = "Running"
            } else if (data.automotive == true){
              self.lbActivityState.text = "Automotive"
            }
          }else{
            self.lbActivityState.text = "No Found"
          }
        }
      }
    }else{
      DispatchQueue.main.async {
        self.lbActivityState.text = "Motion Not Available"
      }
    }
  }
  
  func didChangedState(_ state: PIPState) {
    switch state {
    case .pip:
      print("Pip Mode")
    case .full:
      print("Full Mode")
    }
  }
  
  func didChangePosition(_ position: PIPPosition) {
    switch position {
    case .topLeft:
      print("topLeft")
    case .middleLeft:
      print("middleLeft")
    case .bottomLeft:
      print("bottomLeft")
    case .topRight:
      print("topRight")
    case .middleRight:
      print("middleRight")
    case .bottomRight:
      print("bottomRight")
    }
  }
}

extension PedoMeterVC {
  
  func setView(){
    self.lbTitle.text = "Steps Counter"
    self.lbActivityState.text = "Idle"
    self.lbStepsCount.text = "-"
    self.lbTimer.text = "-"
    
    self.btStop.addTarget(self, action: #selector(self.stopStepCounting), for: .touchUpInside)
    self.btRecord.addTarget(self, action: #selector(self.startStepCounting), for: .touchUpInside)
    
    self.uvCard.layer.cornerRadius = 10
    self.uvCard.layer.shadowColor = UIColor.black.cgColor
    self.uvCard.layer.shadowOpacity = 1
    self.uvCard.layer.shadowOffset = .zero
    self.uvCard.layer.shadowRadius = 2
    self.uvCard.layer.shouldRasterize = true
    self.toggleButton()
  }
  
  func toggleButton(){
    DispatchQueue.main.async {
      if self.recordStarted {
        self.btRecord.isEnabled = false
        self.btStop.isEnabled = true
      }else{
        self.btRecord.isEnabled = true
        self.btStop.isEnabled = false
      }
    }
  }
  
  @objc func startStepCounting(){
    if(CMPedometer.isStepCountingAvailable()){
      self.startTimer()
      self.recordStarted = true
      self.pedoMeter = CMPedometer()
      self.pedoMeter.startUpdates(from: Date()) { (data, error) in
        if let data = data{
          self.numberOfSteps = Int(truncating: data.numberOfSteps)
          
//          if let distance = data.distance {
//            self.distance = Double(truncating: distance)
//          }
//
//          if let averageActivePace = data.averageActivePace {
//            self.averagePace = Double(truncating: averageActivePace)
//          }
//
//          if let currentPace = data.currentPace {
//            self.pace = Double(truncating: currentPace)
//          }
        }else{
//          self.pace = 0
//          self.averagePace = 0
//          self.distance = 0
          self.numberOfSteps = 0
        }
        if let error = error {
          print(error)
        }
      }
    }else{
      self.lbStepsCount.text = "Pedo Meter Not Available"
      self.recordStarted = false
    }
  }
  
  @objc func stopStepCounting(){
    self.pedoMeter.stopUpdates()
    self.recordStarted = false
    DispatchQueue.main.async {
      PIPKit.dismiss(animated: true)
    }
  }
}

//MARK: - timer functions
extension PedoMeterVC {
  func startTimer(){
    if timer.isValid { timer.invalidate() }
    timer = Timer.scheduledTimer(timeInterval: timerInterval,target: self,selector: #selector(timerAction(timer:)) ,userInfo: nil,repeats: true)
  }
  
  func stopTimer(){
    timer.invalidate()
    displayPedometerData()
  }
  
  @objc func timerAction(timer:Timer){
    displayPedometerData()
  }
  // display the updated data
  func displayPedometerData(){
    timeElapsed += 1.0
    self.lbTimer.text = timeIntervalFormat(interval: timeElapsed)
    //Number of steps
    self.lbStepsCount.text = String(format:"Steps: %i",self.numberOfSteps)
    
//    //distance
//    if let distance = self.distance{
//      distanceLabel.text = String(format:"Distance: %02.02f meters,\n %02.02f mi",distance,miles(meters: distance))
//    } else {
//      distanceLabel.text = "Distance: N/A"
//    }
//
//    //average pace
//    if let averagePace = self.averagePace{
//      avgPaceLabel.text = paceString(title: "Avg Pace", pace: averagePace)
//    } else {
//      avgPaceLabel.text =  paceString(title: "Avg Comp Pace", pace: computedAvgPace())
//    }
//
//    //pace
//    if let pace = self.pace {
//      print(pace)
//      paceLabel.text = paceString(title: "Pace:", pace: pace)
//    } else {
//      paceLabel.text = "Pace: N/A "
//      paceLabel.text =  paceString(title: "Avg Comp Pace", pace: computedAvgPace())
//    }
  }
  
  //MARK: - Display and time format functions
  
  // convert seconds to hh:mm:ss as a string
  func timeIntervalFormat(interval:TimeInterval)-> String{
    var seconds = Int(interval + 0.5) //round up seconds
    let hours = seconds / 3600
    let minutes = (seconds / 60) % 60
    seconds = seconds % 60
    return String(format:"%02i:%02i:%02i",hours,minutes,seconds)
  }
  // convert a pace in meters per second to a string with
  // the metric m/s and the Imperial minutes per mile
//  func paceString(title:String,pace:Double) -> String{
//    var minPerMile = 0.0
//    let factor = 26.8224 //conversion factor
//    if pace != 0 {
//      minPerMile = factor / pace
//    }
//    let minutes = Int(minPerMile)
//    let seconds = Int(minPerMile * 60) % 60
//    return String(format: "%@: %02.2f m/s \n\t\t %02i:%02i min/mi",title,pace,minutes,seconds)
//  }
  
//  func computedAvgPace()-> Double {
//    if let distance = self.distance{
//      pace = distance / timeElapsed
//      return pace
//    } else {
//      return 0.0
//    }
//  }
  
//  func miles(meters:Double)-> Double{
//    let mile = 0.000621371192
//    return meters * mile
//  }
}
