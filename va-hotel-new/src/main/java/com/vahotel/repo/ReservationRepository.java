package com.vahotel.repo;
import com.vahotel.model.Reservation;
import com.vahotel.model.Room;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ReservationRepository extends JpaRepository<Reservation,Long>{
 @Query("select count(r) from Reservation r where r.room=:room and r.status in ('PENDING','PAID','APPROVED','CANCELLATION_REQUESTED') and r.checkIn < :outDate and r.checkOut > :inDate")
 long countOverlaps(@Param("room") Room room,@Param("inDate") LocalDate inDate,@Param("outDate") LocalDate outDate);
 List<Reservation> findByCustomerIdOrderByCheckInDesc(Long customerId);
 List<Reservation> findByRoom_IdAndStatusInOrderByCheckInAsc(Long roomId, List<String> statuses);
}
