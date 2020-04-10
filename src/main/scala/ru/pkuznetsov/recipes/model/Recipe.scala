package ru.pkuznetsov.recipes.model

import java.net.URI

final case class Recipe(id: Long,
                        name: String,
                        uri: Option[URI],
                        summary: String,
                        cookingTime: Option[Int],
                        calories: Option[Double],
                        protein: Option[Double],
                        fat: Option[Double],
                        carbohydrates: Option[Double],
                        sugar: Option[Double],
                        ingredients: List[Ingredient])

final case class Ingredient(id: Long,
                            name: String,
                            amount: String,
                            unit: String)