// Bedrock file. DO NOT DELETE OR MODIFY.

package au.com.foxsports.bedrock.model;

import java.util.Objects;

public class Greeting {

    private final Long id;

    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Greeting greeting = (Greeting) o;
        return Objects.equals(id, greeting.id) &&
                Objects.equals(content, greeting.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content);
    }

    @Override
    public String toString() {
        return "Greeting{" + "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
