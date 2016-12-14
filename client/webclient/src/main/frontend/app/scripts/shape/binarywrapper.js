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

function BinaryFileWrapper(binFile) {
    
    this.position = 0;
    this.bigEndian = true;

    this.getByte = function() {
        var byte = binFile.getByteAt(this.position);
        this.position++;
        return byte;
    }

    this.getLength = function() {
        return binFile.getLength();
    }

    this.getSByte = function() {
        var sbyte = binFile.getSByteAt(this.position);
        this.position++;
        return sbyte;
    }

    this.getShort = function() {
        var short = binFile.getShortAt(this.position, this.bigEndian);
        this.position += 2;
        return short;
    }
    
    this.getSShort = function() {
        var sshort = binFile.getSShortAt(this.position, this.bigEndian);
        this.position += 2;
        return sshort;
    }
    
    this.getLong = function() {
        var l = binFile.getLongAt(this.position, this.bigEndian);
        this.position += 4;
        return l;
    }
    
    this.getSLong = function() {
        var l = binFile.getSLongAt(this.position, this.bigEndian);
        this.position += 4;
        return l;
    }
    
    this.getString = function(iLength) {
        var s = binFile.getStringAt(this.position, iLength);
        this.position += iLength;
        return s;
    }

	this.getDoubleAt = function(iOffset, bBigEndian) {
		// hugs stackoverflow
		// http://stackoverflow.com/questions/1597709/convert-a-string-with-a-hex-representation-of-an-ieee-754-double-into-javascript
		// TODO: check the endianness for something other than shapefiles
		// TODO: what about NaNs and Infinity?
		var a = binFile.getLongAt(iOffset + (bBigEndian ? 0 : 4), bBigEndian);
		var b = binFile.getLongAt(iOffset + (bBigEndian ? 4 : 0), bBigEndian);
		var s = a >> 31 ? -1 : 1;
		var e = (a >> 52 - 32 & 0x7ff) - 1023;
		return s * (a & 0xfffff | 0x100000) * 1.0 / Math.pow(2,52-32) * Math.pow(2, e) + b * 1.0 / Math.pow(2, 52) * Math.pow(2, e);
	}

    this.getDouble = function() {    
        var d = this.getDoubleAt(this.position, this.bigEndian);
        this.position += 8;
        return d;
    }

    this.getChar = function() {
        var c = binFile.getCharAt(this.position);
        this.position++;
        return c;
    }
}