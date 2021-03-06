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

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "actiontype", schema = "action")
public class ActionType implements Serializable {

  private static final long serialVersionUID = -581549382631976704L;

  @Id
  @Column(name = "actiontypepk")
  private Integer actionTypePK;

  private String name;
  private String description;
  private String handler;

  @Column(name = "cancancel")
  private Boolean canCancel;


  @Column(name = "responserequired")
  private Boolean responseRequired;

}
