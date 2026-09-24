package com.example.domain.repository

import com.example.domain.model.Money
import com.example.domain.model.PricingSnapshot
import com.example.domain.model.ProductSnapshot
import kotlinx.coroutines.flow.Flow

data class ResolvedProduct(
    val snapshot: ProductSnapshot,
    val pricing: PricingSnapshot
)

interface ProductRepository {
    suspend fun resolveBarcode(barcode: String): ResolvedProduct?
    suspend fun seedInitialCatalogIfEmpty()
    fun getAllProducts(): Flow<List<ResolvedProduct>>
    suspend fun searchCatalog(query: String): List<ResolvedProduct>
    suspend fun saveProduct(product: ResolvedProduct)
}
