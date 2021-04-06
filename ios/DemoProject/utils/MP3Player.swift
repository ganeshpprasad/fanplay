//
//  MP3Player.swift
//  DemoProject
//
//  Created by Sysfore on 30/March/2021.
//

import UIKit
import AVFoundation
import AudioToolbox

public final class MP3Player : NSObject {
  
  // Singleton class
  static let shared:MP3Player = MP3Player()
  
  private var player: AVAudioPlayer? = nil
  
  // Play only mp3 which are stored in the local
  public func playLocalFile(name:String) {
    guard let url = Bundle.main.url(forResource: name, withExtension: "mp3") else { return } 
    do {
      try AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playback)
      try AVAudioSession.sharedInstance().setActive(true)
      player = try AVAudioPlayer(contentsOf: url, fileTypeHint: AVFileType.mp3.rawValue)
      guard let player = player else { return }
      
      player.play()
    }catch let error{
      print(error.localizedDescription)
    }
  }
}
