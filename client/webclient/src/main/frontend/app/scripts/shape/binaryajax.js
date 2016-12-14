/* 
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
 * 
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


var BinaryFile = function(strData, iDataOffset, iDataLength) {
	var data = strData;
	var dataOffset = iDataOffset || 0;
	var dataLength = 0;	
	this.getRawData = function() {
		return data;
	}

	if (typeof strData == "string") {
		dataLength = iDataLength || data.length;

		this.getByteAt = function(iOffset) {
			return data.charCodeAt(iOffset + dataOffset) & 0xFF;
		}
	} else if (typeof strData == "unknown") {
		dataLength = iDataLength || IEBinary_getLength(data);

		this.getByteAt = function(iOffset) {
			return IEBinary_getByteAt(data, iOffset + dataOffset);
		}
	}

	this.getLength = function() {
		return dataLength;
	}

	this.getSByteAt = function(iOffset) {
		var iByte = this.getByteAt(iOffset);
		if (iByte > 127)
			return iByte - 256;
		else
			return iByte;
	}

	this.getShortAt = function(iOffset, bBigEndian) {
		var iShort = bBigEndian ? 
			(this.getByteAt(iOffset) << 8) + this.getByteAt(iOffset + 1)
			: (this.getByteAt(iOffset + 1) << 8) + this.getByteAt(iOffset)
		if (iShort < 0) iShort += 65536;
		return iShort;
	}
	this.getSShortAt = function(iOffset, bBigEndian) {
		var iUShort = this.getShortAt(iOffset, bBigEndian);
		if (iUShort > 32767)
			return iUShort - 65536;
		else
			return iUShort;
	}
	this.getLongAt = function(iOffset, bBigEndian) {
		var iByte1 = this.getByteAt(iOffset),
			iByte2 = this.getByteAt(iOffset + 1),
			iByte3 = this.getByteAt(iOffset + 2),
			iByte4 = this.getByteAt(iOffset + 3);

		var iLong = bBigEndian ? 
			(((((iByte1 << 8) + iByte2) << 8) + iByte3) << 8) + iByte4
			: (((((iByte4 << 8) + iByte3) << 8) + iByte2) << 8) + iByte1;
		if (iLong < 0) iLong += 4294967296;
		return iLong;
	}
	this.getSLongAt = function(iOffset, bBigEndian) {
		var iULong = this.getLongAt(iOffset, bBigEndian);
		if (iULong > 2147483647)
			return iULong - 4294967296;
		else
			return iULong;
	}
	this.getStringAt = function(iOffset, iLength) {
		var aStr = [];
		for (var i=iOffset,j=0;i<iOffset+iLength;i++,j++) {
			aStr[j] = String.fromCharCode(this.getByteAt(i));
		}
		return aStr.join("");
	}

	this.getCharAt = function(iOffset) {
		return String.fromCharCode(this.getByteAt(iOffset));
	}
	this.toBase64 = function() {
		return window.btoa(data);
	}
	this.fromBase64 = function(strBase64) {
		data = window.atob(strBase64);
	}
}



document.write(
	"<script type='text/vbscript'>\r\n"
	+ "Function IEBinary_getByteAt(strBinary, iOffset)\r\n"
	+ "	IEBinary_getByteAt = AscB(MidB(strBinary,iOffset+1,1))\r\n"
	+ "End Function\r\n"
	+ "Function IEBinary_getLength(strBinary)\r\n"
	+ "	IEBinary_getLength = LenB(strBinary)\r\n"
	+ "End Function\r\n"
	+ "</script>\r\n"
);
