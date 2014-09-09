package com.github.kardapoltsev.webgallery.es


import akka.actor.Actor



/**
 * Created by alexey on 9/9/14.
 */
trait EventPublisher { this: Actor =>
  protected def publish(event: AnyRef) = {
    context.system.eventStream.publish(event)
  }
}
