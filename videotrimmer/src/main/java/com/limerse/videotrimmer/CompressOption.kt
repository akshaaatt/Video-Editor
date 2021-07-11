package com.limerse.videotrimmer

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
class CompressOption : Parcelable {
    var frameRate = 30
    var bitRate: String? = "0k"
    var widthHere = 0
    var heightHere = 0

    constructor()
    constructor(frameRate: Int, bitRate: String?, width: Int, height: Int) {
        this.frameRate = frameRate
        this.bitRate = bitRate
        widthHere = width
        heightHere = height
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(frameRate)
        dest.writeString(bitRate)
        dest.writeInt(widthHere)
        dest.writeInt(heightHere)
    }

    constructor(`in`: Parcel) {
        frameRate = `in`.readInt()
        bitRate = `in`.readString()
        widthHere = `in`.readInt()
        heightHere = `in`.readInt()
    }

    companion object CREATOR : Parcelable.Creator<CompressOption> {
        override fun createFromParcel(parcel: Parcel): CompressOption {
            return CompressOption(parcel)
        }

        override fun newArray(size: Int): Array<CompressOption?> {
            return arrayOfNulls(size)
        }
    }
}