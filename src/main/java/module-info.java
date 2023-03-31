module blazingtwist.sod_luadatabasebot {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires java.sql;
	requires org.spongepowered.configurate;
	requires org.spongepowered.configurate.yaml;
	requires org.spongepowered.configurate.hocon;
	requires org.spongepowered.configurate.jackson;
	requires com.google.gson;
	requires reflections;
	requires org.jetbrains.annotations;
	requires fx.loading.spinner;
	requires java.net.http;

	opens blazingtwist.sod_luadatabasebot
			to javafx.fxml,
			org.spongepowered.configurate;

	opens blazingtwist.sod_luadatabasebot.ui
			to javafx.fxml;

	opens blazingtwist.sod_luadatabasebot.yamlmapper.mappers
			to org.spongepowered.configurate;

	opens blazingtwist.sod_luadatabasebot.config
			to org.spongepowered.configurate;

	exports blazingtwist.sod_luadatabasebot;
	exports blazingtwist.sod_luadatabasebot.config;
	exports blazingtwist.sod_luadatabasebot.yamlmapper;
	exports blazingtwist.sod_luadatabasebot.utils;
	exports blazingtwist.sod_luadatabasebot.ui;
}