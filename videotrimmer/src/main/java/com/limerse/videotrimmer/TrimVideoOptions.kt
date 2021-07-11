package com.limerse.videotrimmer

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
class TrimVideoOptions : Parcelable {
    var destination: String? = null
    var fileName: String? = null
    var trimType: TrimType? = TrimType.DEFAULT
    var minDuration: Long = 0
    var fixedDuration: Long = 0
    var hideSeekBar = false
    var accurateCut = false
    var minToMax: LongArray? = null
    var compressOption: CompressOption? = null

    constructor()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(destination)
        dest.writeString(fileName)
        dest.writeInt(if (trimType == null) -1 else trimType!!.ordinal)
        dest.writeLong(minDuration)
        dest.writeLong(fixedDuration)
        dest.writeByte(if (hideSeekBar) 1.toByte() else 0.toByte())
        dest.writeByte(if (accurateCut) 1.toByte() else 0.toByte())
        dest.writeLongArray(minToMax)
        dest.writeParcelable(compressOption, flags)
    }

    constructor(`in`: Parcel) {
        destination = `in`.readString()
        fileName = `in`.readString()
        val tmpTrimType = `in`.readInt()
        trimType = if (tmpTrimType == -1) null else TrimType.values()[tmpTrimType]
        minDuration = `in`.readLong()
        fixedDuration = `in`.readLong()
        hideSeekBar = `in`.readByte().toInt() != 0
        accurateCut = `in`.readByte().toInt() != 0
        minToMax = `in`.createLongArray()
        compressOption = `in`.readParcelable(CompressOption::class.java.classLoader)
    }

    companion object CREATOR : Parcelable.Creator<TrimVideoOptions> {
        override fun createFromParcel(parcel: Parcel): TrimVideoOptions {
            return TrimVideoOptions(parcel)
        }

        override fun newArray(size: Int): Array<TrimVideoOptions?> {
            return arrayOfNulls(size)
        }
    }

}