package com.example.km2mapchecklib;

public class CbManger{
    public interface callbackInterface{
        void Notify(String str);
    }
    private callbackInterface mCbinterface;

    public void SetNotify(callbackInterface cbInterface)
    {
        this.mCbinterface = cbInterface;
    }

    private CbManger()
    {}

    private static class CbMangerHolder
    {
        private final static CbManger instance=new CbManger();
    }

    public static CbManger getInstance(){
        return CbManger.CbMangerHolder.instance;
    }

    public callbackInterface GetCbObject()
    {
        return this.mCbinterface;
    }
}
