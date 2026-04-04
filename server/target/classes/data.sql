insert into transit_stops (mbta_stop_id, name, mode, lat, lng, active, movable)
values
('place-boyls', 'Boylston', 'SUBWAY', 42.353020, -71.064590, true, true),
('place-pktrm', 'Park Street', 'SUBWAY', 42.35639457, -71.0624242, true, true),
('place-dwnxg', 'Downtown Crossing', 'SUBWAY', 42.355518, -71.060225, true, true)
on conflict do nothing;

insert into transit_routes (mbta_route_id, short_name, long_name, mode, color)
values
('Red', 'Red', 'Red Line', 'SUBWAY', 'DA291C'),
('Green-B', 'B', 'Green Line B', 'SUBWAY', '00843D')
on conflict do nothing;

insert into heatmap_cells (area_code, lat, lng, intensity, hour_of_day, metric_type)
values
('downtown', 42.3555, -71.0620, 0.92, 13, 'DEMAND'),
('downtown', 42.3548, -71.0614, 0.76, 13, 'DEMAND'),
('downtown', 42.3560, -71.0630, 0.68, 13, 'DEMAND')
on conflict do nothing;
