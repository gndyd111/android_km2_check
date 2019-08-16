package com.example.km2mapchecklib;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VanField {
    private VanProject vanProject;
    private int NO;
    private LLRECT	 mBR = new LLRECT();
    private List<VanZone> listZone = new ArrayList<>() ;

    public VanField(int no, VanProject vp)
    {
        NO = no;
        vanProject = vp;
    }

    public int GetNo()
    {
        return NO;
    }

    public VanProject GetProject()
    {
        return vanProject;
    }

    public void Insert(VanZone vz)
    {
        for (int i=0; i<vz.listPoints.size(); ++i)
        {
            mBR.Union(vz.listPoints.get(i));
        }

        listZone.add(vz);
    }

    public int GetZoneCount()
    {
        return listZone.size();
    }

    public VanZone GetZone(int index)
    {
        return listZone.get(index);
    }

    public LLRECT GetMBR()
    {
        return mBR;
    }
}
