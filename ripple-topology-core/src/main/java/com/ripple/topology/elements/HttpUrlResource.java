package com.ripple.topology.elements;

import com.ripple.topology.Resource;
import okhttp3.HttpUrl;

/**
 * @author jfulton
 */
public interface HttpUrlResource extends Resource {

    HttpUrl getHttpUrl();
}
