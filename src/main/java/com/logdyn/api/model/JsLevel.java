package com.logdyn.api.model;

import java.util.logging.Level;

import org.json.JSONObject;
import org.json.JSONString;

/**
 * Created by Matt on 01/06/2017.
 */
public class JsLevel extends Level implements JSONString
{
    public static final JsLevel ERROR = new JsLevel("ERROR", 950);

    public static final JsLevel WARN = new JsLevel("WARN", 850);

    private static final long serialVersionUID = -6831164266078074026L;

    private JsLevel(final String name, final int value)
    {
        super(name, value);
    }

    public static Level parse(final String name) throws IllegalArgumentException
    {
        return Level.parse(name);
    }

	@Override
	public String toJSONString()
	{
		return JSONObject.quote(this.getName());
	}
}
