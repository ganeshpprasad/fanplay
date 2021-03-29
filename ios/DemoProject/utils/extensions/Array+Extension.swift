//
//  Array+Extension.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import Foundation

/// The average value of all the items in the array
extension Array where Element: BinaryInteger {
    var average: Double {
        if self.isEmpty {
            return 0.0
        } else {
            let sum = self.reduce(0, +)
            return Double(sum) / Double(self.count)
        }
    }
}


/// The average value of all the items in the array
extension Array where Element: BinaryFloatingPoint {
    var average: Double {
        if self.isEmpty {
            return 0.0
        } else {
            let sum = self.reduce(0, +)
            return Double(sum) / Double(self.count)
        }
    }
}
