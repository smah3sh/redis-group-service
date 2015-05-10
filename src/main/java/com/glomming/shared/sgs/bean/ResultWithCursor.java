package com.glomming.shared.sgs.bean;

import java.util.Collection;

/**
 * Created by smahesh on 5/9/15.
 */
public class ResultWithCursor<T> {
  public String cursor;
  public Collection<T> results;
}
