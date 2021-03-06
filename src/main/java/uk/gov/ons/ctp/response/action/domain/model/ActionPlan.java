package uk.gov.ons.ctp.response.action.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "actionplan", schema = "action")
public class ActionPlan implements Serializable {

  private static final long serialVersionUID = 3621028547635970347L;

  @Id
  @Column(name = "actionplanpk")
  private Integer actionPlanPK;

  private UUID id;

  private String name;

  private String description;

  @Column(name = "createdby")
  private String createdBy;

  @Column(name = "lastrundatetime")
  private Timestamp lastRunDateTime;

}
