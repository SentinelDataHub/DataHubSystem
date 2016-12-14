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

function DbfFile(binFile) {

    this.src = new BinaryFileWrapper(binFile);

    var t1 = new Date().getTime();    

    this.header = new DbfHeader(this.src);

    var t2 = new Date().getTime();
    if (window.console && window.console.log) console.log('parsed dbf header in ' + (t2-t1) + ' ms');    

    t1 = new Date().getTime();    
    
    // TODO: could maybe be smarter about this and only parse these on demand
    this.records = [];
    for (var i = 0; i < this.header.recordCount; i++) {
        var record = this.getRecord(i);
        this.records.push(record);
    }    

    t2 = new Date().getTime();
    if (window.console && window.console.log) console.log('parsed dbf records in ' + (t2-t1) + ' ms');    
    
}
DbfFile.prototype.getRecord = function(index) {

    if (index > this.header.recordCount) {
        throw(new DbfError("",DbfError.ERROR_OUTOFBOUNDS));
    }

    this.src.position = this.header.recordsOffset + index * this.header.recordSize;
    this.src.bigEndian = false;

    return new DbfRecord(this.src, this.header);
}


function DbfHeader(src) {
    
    // endian:
    src.bigEndian = false;

    this.version = src.getSByte();
    this.updateYear = 1900+src.getByte();
    this.updateMonth = src.getByte();
    this.updateDay = src.getByte();
    this.recordCount = src.getLong();
    this.headerSize = src.getShort();
    this.recordSize = src.getShort();

    //skip 2:
    src.position += 2;

    this.incompleteTransaction = src.getByte();
    this.encrypted = src.getByte();

    // skip 12:
    src.position += 12;

    this.mdx = src.getByte();
    this.language = src.getByte();

    // skip 2;
    src.position += 2;

    // iterate field descriptors:
    this.fields = [];
    while (src.getSByte() != 0x0D){
        src.position -= 1;
        this.fields.push(new DbfField(src));
    }

    this.recordsOffset = this.headerSize+1;                                                                    
    
}                

function DbfField(src) {

    this.name = this.readZeroTermANSIString(src);

    // fixed length: 10, so:
    src.position += (10-this.name.length);

    this.type = src.getByte();
    this.address = src.getLong();
    this.length = src.getByte();
    this.decimals = src.getByte();

    // skip 2:
    src.position += 2;

    this.id = src.getByte();

    // skip 2:
    src.position += 2;

    this.setFlag = src.getByte();

    // skip 7:
    src.position += 7;

    this.indexFlag = src.getByte();
}
DbfField.prototype.readZeroTermANSIString = function(src) {
    var r = [];
    var b;
    while (b = src.getByte()) {
        r[r.length] = String.fromCharCode(b);
    }
    return r.join('');
}

function DbfRecord(src, header) {
    this.offset = src.position;
    this.values = {}
    for (var i = 0; i < header.fields.length; i++) {
        var field = header.fields[i];
        this.values[field.name] = src.getString(field.length);
    }                             
}