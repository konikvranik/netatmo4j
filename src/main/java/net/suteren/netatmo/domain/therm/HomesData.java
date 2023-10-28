package net.suteren.netatmo.domain.therm;

import java.util.List;

public record HomesData(
	List<Home> homes,
	User user) {
}
