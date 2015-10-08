/**
 *              COPYRIGHT (C) 2008-2014 SERVICEMESH, INC.
 *                        ALL RIGHTS RESERVED.
 *                   CONFIDENTIAL AND PROPRIETARY.
 *
 *  ALL SOFTWARE, INFORMATION AND ANY OTHER RELATED COMMUNICATIONS
 *  (COLLECTIVELY, "WORKS") ARE CONFIDENTIAL AND PROPRIETARY INFORMATION THAT
 *  ARE THE EXCLUSIVE PROPERTY OF SERVICEMESH.
 *  ALL WORKS ARE PROVIDED UNDER THE APPLICABLE AGREEMENT OR END USER LICENSE
 *  AGREEMENT IN EFFECT BETWEEN YOU AND SERVICEMESH.  UNLESS OTHERWISE SPECIFIED
 *  IN THE APPLICABLE AGREEMENT, ALL WORKS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 *  OF ANY KIND EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 *  ALL USE, DISCLOSURE AND/OR REPRODUCTION OF WORKS NOT EXPRESSLY AUTHORIZED BY
 *  SERVICEMESH IS STRICTLY PROHIBITED.
 */

package com.servicemesh.io.http;

public enum HttpMethod
{
    OPTIONS(0, "OPTIONS"),
    GET(1, "GET"),
    HEAD(2, "HEAD"),
    POST(3, "POST"),
    PUT(4, "PUT"),
    DELETE(5, "DELETE"),
    TRACE(6, "TRACE"),
    CONNECT(7, "CONNECT");

    final private int _id;
    final private String _name;

    private HttpMethod(int id, String name)
    {
        _id = id;
        _name = name;
    }

    public String getName()
    {
        return _name.toUpperCase();
    }

    public int getId()
    {
        return _id;
    }

    @Override
    public String toString()
    {
        return _name;
    }
}
