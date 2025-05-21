-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mer. 21 mai 2025 à 10:53
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `medi_blog`
--
CREATE DATABASE IF NOT EXISTS `medi_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `medi_blog`;

-- --------------------------------------------------------

--
-- Structure de la table `blog`
--

CREATE TABLE `blog` (
  `id` bigint(20) NOT NULL,
  `contenu` text DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `titre` varchar(255) DEFAULT NULL,
  `auteur_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `blog`
--

INSERT INTO `blog` (`id`, `contenu`, `created_at`, `image`, `titre`, `auteur_id`) VALUES
(1, '\r\nL’activité physique régulière joue un rôle essentiel dans la prévention des maladies cardiovasculaires. Des études montrent qu’au moins 150 minutes d’exercice modéré par semaine peuvent réduire significativement le risque d’hypertension, d’athérosclérose, et d’infarctus du myocarde. L’exercice améliore également le profil lipidique et favorise la régulation de la glycémie, contribuant à limiter le développement du diabète de type 2, un facteur de risque cardiovasculaire majeur. Il est recommandé d’intégrer des activités variées, telles que la marche rapide, la natation ou le vélo, adaptées à l’âge et à l’état de santé du patient.', '2025-05-21 08:34:56.000000', NULL, 'Impact de l’activité physique sur la prévention des maladies cardiovasculaires', 1),
(2, ' Le diabète de type 2 est une maladie métabolique chronique caractérisée par une résistance à l’insuline et une hyperglycémie persistante. Récemment, de nouveaux médicaments ont été développés, notamment les inhibiteurs du SGLT2 et les agonistes du GLP-1. Ces traitements améliorent le contrôle glycémique tout en offrant des bénéfices cardiovasculaires et rénaux. Par ailleurs, ils contribuent souvent à une perte de poids, un avantage important pour la gestion globale du diabète. Leur intégration dans la pratique clinique nécessite une évaluation personnalisée des patients afin d’optimiser les résultats thérapeutiques.', '2025-05-21 08:35:24.000000', NULL, 'Nouveaux traitements dans la prise en charge du diabète de type 2', 1),
(3, 'Avec le vieillissement, le système immunitaire subit un déclin naturel appelé immunosénescence, augmentant la vulnérabilité aux infections. La vaccination reste un outil clé pour prévenir les maladies infectieuses graves chez les personnes âgées, notamment la grippe saisonnière, le pneumocoque et le zona. La vaccination contre la grippe, par exemple, réduit la mortalité liée aux complications infectieuses et diminue les hospitalisations. Il est donc recommandé d’intégrer systématiquement ces vaccinations dans le suivi médical des patients âgés, en adaptant les schémas vaccinaux selon les comorbidités.', '2025-05-21 08:35:49.000000', NULL, 'L’importance de la vaccination chez les personnes âgées', 1);

-- --------------------------------------------------------

--
-- Structure de la table `comment`
--

CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL,
  `content` text NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `author_id` bigint(20) NOT NULL,
  `blog_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `bio` varchar(1000) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `bio`, `email`, `password`, `profile_picture`, `username`) VALUES
(1, 'ETUDIANTE EN MEDECINE TREEES DOUE', 'jihane@gmail.com', '$2a$10$07r4kN.oT1uoibfxcgKw6uYBYR6lVWSWbNXWIwO1Y6Dn3UkdFvl3e', NULL, 'Jihane'),
(2, 'Doctor to be ', 'aya@gmail.com', '$2a$10$R6Co1Ot55Gticym1ryNmdeV7.FznJu2UwK43qB5bJKUNE7TfTGJAe', NULL, 'Aya Idouahmane');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `blog`
--
ALTER TABLE `blog`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKpoy0brbbjsubgtfqb0ojfxcvj` (`auteur_id`);

--
-- Index pour la table `comment`
--
ALTER TABLE `comment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKir20vhrx08eh4itgpbfxip0s1` (`author_id`),
  ADD KEY `FKkap39f76wn135k7ru564fbjb7` (`blog_id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `blog`
--
ALTER TABLE `blog`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `comment`
--
ALTER TABLE `comment`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `blog`
--
ALTER TABLE `blog`
  ADD CONSTRAINT `FKpoy0brbbjsubgtfqb0ojfxcvj` FOREIGN KEY (`auteur_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `comment`
--
ALTER TABLE `comment`
  ADD CONSTRAINT `FKir20vhrx08eh4itgpbfxip0s1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKkap39f76wn135k7ru564fbjb7` FOREIGN KEY (`blog_id`) REFERENCES `blog` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
