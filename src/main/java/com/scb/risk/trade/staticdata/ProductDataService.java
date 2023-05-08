package com.scb.risk.trade.staticdata;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.scb.risk.trade.model.ProductData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Reference data implementation for <link>ProductData</link>, essentially
 * provides singleton interface to init and read the static product data
 * and exposes end point to read the static data.
 *
 * @See ProductData
 */
public class ProductDataService implements ReferenceData<ProductData> {
    private static Logger log = LoggerFactory.getLogger(ProductDataService.class);

    private static final String PRODUCT_RESOURCE = "product.csv";

    private static final Map<Long, ProductData> referenceDataCache = Maps.newHashMapWithExpectedSize(100);

    private final static ProductDataService INSTANCE = new ProductDataService();

    ProductDataService() {
        init();
    }

    public static ProductDataService getInstance() {
        return INSTANCE;
    }

    private void init()  {

        Path path = Paths.get(Resources.getResource(PRODUCT_RESOURCE).getPath());
        log.info("Loading the product reference cache from path resource, {}", path);

        if (!Files.exists(path)) {
            log.error("Product file doesnt exist, {}", path);
            return;
        }

        try {

            Files.lines(path).forEach(data -> {
                log.info("Processing the raw product data adding to reference cache, {}", data);
                if (data!= null && !data.startsWith("product_id")) {
                    String[] tokenizedData = data.split(",");
                    ProductData productData = new ProductData(Long.parseLong(tokenizedData[0]), tokenizedData[1]);
                    log.info("Adding to the product data reference cache, data {}", productData);
                    referenceDataCache.put(productData.getId(), productData);
                }
            });

            log.info("Loading product reference cache completed.");

        } catch (IOException e) {
            log.error("Error when initializing reference data, {}", e);
            throw new RuntimeException(e);
        }

    }

    public ProductData getProduct(Long productId) {
        ProductData productData = referenceDataCache.get(productId);
        log.info("Retrieved the product data, {}, for the id, {}", productData, productId);
        return productData;
    }


}
