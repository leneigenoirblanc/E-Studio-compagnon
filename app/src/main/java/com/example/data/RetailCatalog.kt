package com.example.data

/**
 * Référentiel local d'articles de grande distribution pour démonstration et enrichissement immédiat
 */
object RetailCatalog {

    data class ProductDescriptor(
        val code: String,
        val designation: String,
        val price: Double,
        val promoPrice: Double? = null,
        val facing: Int = 1
    )

    private val catalog = mapOf(
        "3017620422003" to ProductDescriptor("3017620422003", "Pâte à Tartiner Nutella 400g", 3.49, 2.99, 2),
        "5449000000996" to ProductDescriptor("5449000000996", "Coca-Cola Original Bouteille 1.5L", 1.89, null, 3),
        "8076809513753" to ProductDescriptor("8076809513753", "Pâtes Barilla Spaghetti N°5 500g", 1.25, 0.99, 4),
        "3228857000166" to ProductDescriptor("3228857000166", "Huile d'Olive Vierge Extra Puget 75cl", 8.90, 7.50, 1),
        "3168930010265" to ProductDescriptor("3168930010265", "Café Moulu Carte Noire Familial 250g", 3.75, null, 2),
        "3046920022606" to ProductDescriptor("3046920022606", "Chocolat Lindt Excellence Noir 70% 100g", 2.19, 1.79, 3),
        "3033490004523" to ProductDescriptor("3033490004523", "Yaourts Danone Velouté Nature x8", 2.65, null, 2),
        "3021760400030" to ProductDescriptor("3021760400030", "Biscuits Lu Véritable Petit Beurre 200g", 1.45, 1.15, 2),
        "3560070817342" to ProductDescriptor("3560070817342", "Lait Demi-Écrémé UHT Brique 1L", 1.09, null, 4),
        "3229820129488" to ProductDescriptor("3229820129488", "Fromage Comté AOP 6 Mois 250g", 4.30, 3.85, 2)
    )

    fun findProduct(code: String): ProductDescriptor? {
        return catalog[code.trim()]
    }

    fun getAllDemoProducts(): List<ProductDescriptor> {
        return catalog.values.toList()
    }
}
