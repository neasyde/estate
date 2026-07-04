package com.financeapp.core.ui.icons

/** Russian keyword synonyms per pickable icon, so the picker is searchable in Russian. */
val iconKeywordsRu: Map<String, String> = mapOf(
    "restaurant" to "еда ресторан кафе обед питание",
    "fastfood" to "фастфуд бургер еда перекус",
    "local_cafe" to "кофе кафе чай напиток",
    "directions_car" to "машина авто автомобиль транспорт",
    "directions_bus" to "автобус транспорт проезд",
    "train" to "поезд электричка метро транспорт",
    "flight" to "самолёт полёт путешествие отпуск билет",
    "local_gas_station" to "бензин заправка топливо",
    "medical_services" to "здоровье медицина врач аптека лекарства",
    "fitness_center" to "спорт зал фитнес тренировка",
    "sports_esports" to "игры развлечения приставка",
    "movie" to "кино фильм развлечения",
    "music_note" to "музыка",
    "checkroom" to "одежда шоппинг гардероб",
    "phone" to "телефон связь мобильный",
    "wifi" to "интернет вайфай связь",
    "home" to "дом жильё аренда квартплата коммуналка",
    "bolt" to "электричество свет энергия",
    "school" to "образование учёба школа университет",
    "work" to "работа зарплата офис",
    "laptop" to "ноутбук компьютер фриланс техника",
    "trending_up" to "инвестиции рост акции доход",
    "account_balance" to "банк счёт вклад",
    "credit_card" to "карта кредит оплата",
    "savings" to "накопления копилка сбережения",
    "wallet" to "кошелёк деньги наличные",
    "card_giftcard" to "подарок сертификат",
    "redeem" to "подарок бонус награда",
    "celebration" to "праздник вечеринка",
    "pets" to "питомец животное кот собака",
    "favorite" to "любимое сердце",
    "star" to "избранное звезда",
    "build" to "ремонт инструменты стройка",
    "more_horiz" to "другое прочее ещё",
)

/** True if [name] matches [query] by icon name substring OR any Russian keyword. Blank query matches all. */
fun iconMatches(name: String, query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    if (name.lowercase().contains(q)) return true
    return iconKeywordsRu[name]?.contains(q) == true
}
