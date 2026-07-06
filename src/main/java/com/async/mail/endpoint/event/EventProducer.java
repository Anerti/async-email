package com.async.mail.endpoint.event;

import com.async.mail.endpoint.event.model.PojaEvent;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventProducer<T extends PojaEvent> implements Consumer<List<T>> {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void accept(List<T> events) {
    events.forEach(eventPublisher::publishEvent);
  }
}
