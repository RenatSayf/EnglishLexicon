package com.myapp.lexicon.playlist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ренат on 05.04.2016.
 */
public class ItemListDict
{
    private String _dictName;
    private Boolean _is_selected;
    public ItemListDict(String dictName, Boolean is_selected)
    {
        _dictName = dictName;
        _is_selected = is_selected;
    }

    public String get_dictName()
    {
        return _dictName;
    }

    public void set_dictName(String _dictName)
    {
        this._dictName = _dictName;
    }

    public Boolean get_is_selected()
    {
        return _is_selected;
    }

    public void set_is_selected(Boolean _is_selected)
    {
        this._is_selected = _is_selected;
    }


}
