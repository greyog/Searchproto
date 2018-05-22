package com.greyogproducts.greyog.searchproto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 06/03/2018.
 */

class MyResponseResult {

    @Override
    public String toString() {
        return "All : " + all.toString();
    }

    @SerializedName("All")
    @Expose
    public List<All> all = new ArrayList<>();

    class All {
        @SerializedName("pair_ID")
        @Expose
        public Integer pairID;
        @SerializedName("tab_ID")
        @Expose
        public String tabID;
        @SerializedName("popularity_rank")
        @Expose
        public Integer popularityRank;
        @SerializedName("link")
        @Expose
        public String link;
        @SerializedName("symbol")
        @Expose
        public String symbol;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("trans_name")
        @Expose
        public String transName;
        @SerializedName("pair_type")
        @Expose
        public String pairType;
        @SerializedName("exchange_name_short")
        @Expose
        public String exchangeNameShort;
        @SerializedName("pair_type_label")
        @Expose
        public String pairTypeLabel;
        @SerializedName("aql_link")
        @Expose
        public String aqlLink;
        @SerializedName("aql_pre_link")
        @Expose
        public String aqlPreLink;
        @SerializedName("country_ID")
        @Expose
        public Integer countryID;
        @SerializedName("flag")
        @Expose
        public String flag;
        @SerializedName("exchange_popular_symbol")
        @Expose
        public String exchangePopularSymbol;
        @SerializedName("override_country_ID")
        @Expose
        public Integer overrideCountryID;

        @Override
        public String toString() {
            return " pairID : " + pairID +
                    "; tabID : " + tabID +
                    "; popularityRank : " + popularityRank +
                    "; link : " + link +
                    "; symbol : " + symbol +
                    "; name : " + name +
                    "; transName : " + transName +
                    "; pairType : " + pairType +
                    "; exchangeNameShort : " + exchangeNameShort +
                    "; pairTypeLabel : " + pairTypeLabel +
                    "; aqlLink : " + aqlLink +
                    "; aqlPreLink : " + aqlPreLink +
                    "; countryID : " + countryID +
                    "; flag : " + flag +
                    "; exchangePopularSymbol : " + exchangePopularSymbol +
                    "; overrideCountryID : " + overrideCountryID;
        }
    }
}



