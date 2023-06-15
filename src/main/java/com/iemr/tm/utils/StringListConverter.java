/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.tm.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringListConverter implements AttributeConverter<List<Integer>, String> {

  @Override
  public String convertToDatabaseColumn(List<Integer> list) {
    // Java 8
	  StringBuilder strbul  = new StringBuilder();
	  if(list!=null && list.size()>0){
		  Iterator<Integer> iter = list.iterator();
		     while(iter.hasNext())
		     {
		         strbul.append(iter.next());
		        if(iter.hasNext()){
		         strbul.append(",");
		        }
		     } 
	  }else{
		  return null;
	  }
	     
	 return strbul.toString();
//    return String.join(",", list);
  }

  @Override
  public List<Integer> convertToEntityAttribute(String joined) {
	  List<Integer> list=new ArrayList();
	  if(joined!=null){
		  String[] strArray = joined.split(",");
		  for(int i = 0; i < strArray.length; i++) {
			  list.add( Integer.parseInt(strArray[i]));
		  }  
	  }
	  
    return list;
  }

}