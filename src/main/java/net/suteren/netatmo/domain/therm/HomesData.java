package net.suteren.netatmo.domain.therm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record HomesData(
	List<Home> homes,
	User user
) {

	@JsonIgnore public Optional<Home> getHomeById(String homeId) {
		return homes.stream()
			.filter(h -> Objects.equals(h.id(), homeId))
			.findAny();
	}
}
