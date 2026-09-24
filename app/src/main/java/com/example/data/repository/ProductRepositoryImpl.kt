package com.example.data.repository

import com.example.data.local.dao.ProductDao
import com.example.data.local.entities.ProductEntity
import com.example.domain.model.Money
import com.example.domain.model.PricingSnapshot
import com.example.domain.model.ProductSnapshot
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.ResolvedProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {

    override suspend fun resolveBarcode(barcode: String): ResolvedProduct? {
        val entity = productDao.getProductByBarcode(barcode.trim()) ?: return null
        return entity.toDomain()
    }

    override fun getAllProducts(): Flow<List<ResolvedProduct>> {
        return productDao.getAllProducts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun searchCatalog(query: String): List<ResolvedProduct> {
        return productDao.searchProducts(query).map { it.toDomain() }
    }

    override suspend fun saveProduct(product: ResolvedProduct) {
        productDao.insertProduct(product.toEntity())
    }

    override suspend fun seedInitialCatalogIfEmpty() {
        val count = productDao.countProducts()
        if (count == 0) {
            val seedItems = listOf(
                ProductEntity(
                    barcode = "3017620422003",
                    sku = "SKU-NUTELLA-400",
                    designation = "Nutella Pâte à Tartiner 400g",
                    category = "Épicerie Sucrée",
                    department = "Épicerie",
                    regularPriceMinor = 389,
                    promoPriceMinor = 299,
                    currency = "EUR",
                    promotionId = "PROMO_FLASH_WEEK",
                    facing = 4
                ),
                ProductEntity(
                    barcode = "3274080005003",
                    sku = "SKU-EVIAN-150",
                    designation = "Eau Minérale Naturelle Evian 1.5L",
                    category = "Boissons & Eaux",
                    department = "Liquides",
                    regularPriceMinor = 95,
                    promoPriceMinor = null,
                    currency = "EUR",
                    facing = 6
                ),
                ProductEntity(
                    barcode = "5449000000996",
                    sku = "SKU-COCA-ORIG-15",
                    designation = "Coca-Cola Goût Original 1.5L",
                    category = "Sodas & Boissons Fraîches",
                    department = "Liquides",
                    regularPriceMinor = 189,
                    promoPriceMinor = 149,
                    currency = "EUR",
                    promotionId = "PROMO_SODA_PACK",
                    facing = 5
                ),
                ProductEntity(
                    barcode = "7622210449283",
                    sku = "SKU-LU-GRANOLA-200",
                    designation = "Granola Chocolat au Lait LU 200g",
                    category = "Biscuits",
                    department = "Épicerie",
                    regularPriceMinor = 215,
                    promoPriceMinor = null,
                    currency = "EUR",
                    facing = 3
                ),
                ProductEntity(
                    barcode = "8000500310427",
                    sku = "SKU-KINDER-BUENO-3",
                    designation = "Kinder Bueno Pack x3 barres",
                    category = "Confiserie & Chocolat",
                    department = "Épicerie",
                    regularPriceMinor = 249,
                    promoPriceMinor = 199,
                    currency = "EUR",
                    promotionId = "PROMO_GOUTER_KIDS",
                    facing = 4
                ),
                ProductEntity(
                    barcode = "3033710065967",
                    sku = "SKU-DANONE-VELOUTE-8",
                    designation = "Danone Velouté Nature x8 pots",
                    category = "Produits Laitiers",
                    department = "Frais",
                    regularPriceMinor = 275,
                    promoPriceMinor = null,
                    currency = "EUR",
                    facing = 2
                ),
                ProductEntity(
                    barcode = "3168930010265",
                    sku = "SKU-BONDUELLE-MAIS-300",
                    designation = "Bonduelle Maïs Doux Sans Sucres Ajoutés 300g",
                    category = "Conserves Légumes",
                    department = "Épicerie",
                    regularPriceMinor = 165,
                    promoPriceMinor = 129,
                    currency = "EUR",
                    facing = 3
                ),
                ProductEntity(
                    barcode = "3560070498765",
                    sku = "SKU-LAVAZZA-QUALITA-ORO",
                    designation = "Café en Grains Lavazza Qualità Oro 500g",
                    category = "Cafés & Boissons Chaudes",
                    department = "Épicerie",
                    regularPriceMinor = 649,
                    promoPriceMinor = 499,
                    currency = "EUR",
                    promotionId = "PROMO_CAFE_OR",
                    facing = 2
                )
            )
            productDao.insertProducts(seedItems)
        }
    }

    private fun ProductEntity.toDomain(): ResolvedProduct {
        return ResolvedProduct(
            snapshot = ProductSnapshot(
                sku = sku,
                barcode = barcode,
                designation = designation,
                category = category,
                department = department,
                facing = facing
            ),
            pricing = PricingSnapshot(
                regularPrice = Money(regularPriceMinor, currency),
                promoPrice = promoPriceMinor?.let { Money(it, currency) },
                currency = currency,
                promotionId = promotionId
            )
        )
    }

    private fun ResolvedProduct.toEntity(): ProductEntity {
        return ProductEntity(
            barcode = snapshot.barcode,
            sku = snapshot.sku,
            designation = snapshot.designation,
            category = snapshot.category,
            department = snapshot.department,
            regularPriceMinor = pricing.regularPrice.minorUnits,
            promoPriceMinor = pricing.promoPrice?.minorUnits,
            currency = pricing.currency,
            promotionId = pricing.promotionId,
            facing = snapshot.facing
        )
    }
}
