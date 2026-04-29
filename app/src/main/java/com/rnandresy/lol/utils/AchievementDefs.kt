package com.rnandresy.lol.utils

data class AchievementDef(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val color: String,
    val rarity: String  // "commun" | "rare" | "épique" | "légendaire"
)

val ALL_ACHIEVEMENTS = listOf(
    AchievementDef("first_post",    "📢", "Première Rumeur",     "Premier post publié !",                    "#E91E63", "commun"),
    AchievementDef("ten_posts",     "🎤", "Ragoteur Pro",        "10 posts publiés 👀",                      "#9C27B0", "rare"),
    AchievementDef("twenty_five_p", "📣", "Légende du Campus",   "25 posts, tout le monde te connaît !",     "#3F51B5", "épique"),
    AchievementDef("first_react",   "❤️", "Premier Fan",         "Quelqu'un a réagi à ton post !",           "#F44336", "commun"),
    AchievementDef("popular",       "⭐", "Populaire",           "50 réactions reçues, t'es une star !",     "#FF9800", "rare"),
    AchievementDef("viral",         "🚀", "Viral",               "200 réactions reçues, tu fais l'actu !",  "#FF6D00", "légendaire"),
    AchievementDef("commentator",   "💬", "Grand Bavard",        "20 commentaires écrits",                   "#2196F3", "commun"),
    AchievementDef("confessor",     "🎭", "L'Anonyme",           "Première confession publiée",              "#607D8B", "commun"),
    AchievementDef("dark_confessor","🕵️","Agent Secret",        "5 confessions, qui es-tu vraiment ?",     "#263238", "rare"),
    AchievementDef("poll_creator",  "📊", "Sondeur",             "3 sondages créés",                        "#009688", "commun"),
    AchievementDef("poll_master",   "🗳️","Démocrate",           "10 sondages, tu veux tout savoir !",      "#00695C", "rare"),
    AchievementDef("social",        "🤝", "Social",              "5 conversations démarrées",               "#4CAF50", "commun"),
    AchievementDef("storyteller",   "📖", "Conteur",             "5 stories publiées",                      "#FF5722", "commun"),
    AchievementDef("streak_3",      "🔥", "Sur une lancée",      "Actif 3 jours d'affilée",                 "#FF6D00", "commun"),
    AchievementDef("streak_7",      "💥", "Increvable",          "7 jours actifs, tu dors jamais ?",        "#D50000", "épique"),
    AchievementDef("eni_pride",     "🎓", "Fier(e) de l'ENI",   "Badge ENI obtenu !",                      "#1565C0", "commun"),
    AchievementDef("badge_maker",   "🏷️","Créateur de Badge",   "Tu as créé ton propre badge !",           "#7C4DFF", "rare")
)