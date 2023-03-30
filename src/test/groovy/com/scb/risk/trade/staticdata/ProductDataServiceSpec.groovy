package com.scb.risk.trade.staticdata

import com.scb.risk.trade.model.ProductData
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ProductDataServiceSpec extends Specification {

    void "when product data service inits, check for validity of the product data for [#product_id, #product_name]"() {

        given:
            ReferenceData<ProductData> productData = ProductDataService.getInstance()

        when:
            ProductData actualData = productData.getProduct(product_id)

        then:
            actualData != null
            actualData.name == product_name

        where:
            product_id  | product_name
            1           | 'Treasury Bills Domestic'
            2           | 'Corporate Bonds Domestic'
            3           | 'REPO Domestic'
            4           | 'Interest rate swaps International'
            5           | 'OTC Index Option'
            6           | 'Currency Options'
            7           | 'Reverse Repos International'
            8           | 'REPO International'
            9           | '766A_CORP BD'
            10          | '766B_CORP BD'

    }


}
