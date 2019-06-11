package com.ripple.topology;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author jfulton
 */
@JsonTypeInfo(use= Id.NAME, property = "type")
public interface Element {

}
