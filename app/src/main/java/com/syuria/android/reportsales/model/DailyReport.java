package com.syuria.android.reportsales.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by HP on 24/11/2016.
 */

public class DailyReport implements Serializable {
    String kode_laporan;
    String tanggal;
    BigDecimal ccm;

    public DailyReport(){

    }

    public DailyReport(String kode_laporan,String tanggal, BigDecimal ccm) {
        this.kode_laporan = kode_laporan;
        this.tanggal = tanggal;
        this.ccm = ccm;
    }

    public String getKode_laporan() {
        return kode_laporan;
    }

    public void setKode_laporan(String kode_laporan) {
        this.kode_laporan = kode_laporan;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public BigDecimal getCcm() {
        return ccm;
    }

    public void setCcm(BigDecimal ccm) {
        this.ccm = ccm;
    }
}
