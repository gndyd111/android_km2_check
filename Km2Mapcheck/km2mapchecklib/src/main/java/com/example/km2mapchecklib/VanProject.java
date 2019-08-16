package com.example.km2mapchecklib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VanProject {
    private String ID;
    private Map<Integer, VanField> mapField = new HashMap<Integer, VanField>();

    public VanProject(String id)
    {
        ID = id;
    }

    public void Insert(int nFieldNo, VanZone zone)
    {
        if(!mapField.containsKey(nFieldNo))
        {
            VanField vanField = new VanField(nFieldNo, this);
            mapField.put(nFieldNo, vanField);

            vanField.Insert(zone);
        }
        else
        {
            VanField vanField = mapField.get(nFieldNo);
            vanField.Insert(zone);
        }
    }

    public List<VanField> GetFieldList()
    {
        List<VanField> list = new ArrayList();
        for (Map.Entry<Integer, VanField> entry : mapField.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    public String GetId()
    {
        return ID;
    }
}
