package com.dtc.core.lifecycle;

/**
 * 閻㈢喎鎳￠崨銊︽埂缂佸嫪娆㈤幒銉ュ經
 * 鐎规矮绠熺紒鍕閻ㄥ嫮鏁撻崨钘夋噯閺堢喐鏌熷▔? * 
 * @author Network Service Template
 */
public interface LifecycleComponent {

    /**
     * 閸氼垰濮╃紒鍕
     * 
     * @throws Exception 閸氼垰濮╁鍌氱埗
     */
    void start() throws Exception;

    /**
     * 閸嬫粍顒涚紒鍕
     * 
     * @throws Exception 閸嬫粍顒涘鍌氱埗
     */
    void stop() throws Exception;
}
